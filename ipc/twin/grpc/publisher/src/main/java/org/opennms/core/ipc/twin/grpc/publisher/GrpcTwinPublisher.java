/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.ipc.twin.grpc.publisher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.function.BiConsumer;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.cloud.grpc.minion.TwinRequestProto;
import org.opennms.cloud.grpc.minion.TwinResponseProto;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.twin.api.TwinStrategy;
import org.opennms.core.ipc.twin.common.AbstractTwinPublisher;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriber;
import org.opennms.core.ipc.twin.common.TwinRequest;
import org.opennms.core.ipc.twin.common.TwinUpdate;
import org.opennms.horizon.core.lib.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.grpc.stub.StreamObserver;

public class GrpcTwinPublisher extends AbstractTwinPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcTwinPublisher.class);
    private Multimap<String, StreamObserver<TwinResponseProto>> sinkStreamsByLocation = LinkedListMultimap.create();
    private Map<String, StreamObserver<TwinResponseProto>> sinkStreamsBySystemId = new HashMap<>();
    private final ThreadFactory twinRpcThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("twin-rpc-handler-%d")
            .build();
    private final ExecutorService twinRpcExecutor = Executors.newCachedThreadPool(twinRpcThreadFactory);

    public GrpcTwinPublisher(LocalTwinSubscriber twinSubscriber, GrpcIpcServer grpcIpcServer) {
        super(twinSubscriber);
    }

    @Override
    protected void handleSinkUpdate(TwinUpdate sinkUpdate) {
        sendTwinResponseForSink(mapTwinResponse(sinkUpdate));
    }

    private synchronized boolean sendTwinResponseForSink(TwinResponseProto twinResponseProto) {
        if (sinkStreamsByLocation.isEmpty()) {
            return false;
        }
        try {
            if (Strings.isNullOrEmpty(twinResponseProto.getLocation())) {
                LOG.debug("Sending sink update for key {} at all locations", twinResponseProto.getConsumerKey());
                sinkStreamsByLocation.values().forEach(stream -> {
                    stream.onNext(twinResponseProto);
                });
            } else {
                String location = twinResponseProto.getLocation();
                sinkStreamsByLocation.get(location).forEach(stream -> {
                    stream.onNext(twinResponseProto);
                    LOG.debug("Sending sink update for key {} at location {}", twinResponseProto.getConsumerKey(), twinResponseProto.getLocation());
                });
            }
        } catch (Exception e) {
            LOG.error("Error while sending Twin response for Sink stream", e);
        }
        return true;
    }

    public void start() throws IOException {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(GrpcIpcUtils.LOG_PREFIX)) {
            LOG.info("Activated Twin Service");
        }
    }

    public void close() throws IOException {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(TwinStrategy.LOG_PREFIX)) {
            twinRpcExecutor.shutdown();
            LOG.info("Stopped Twin GRPC Server");
        }
    }

    static class AdapterObserver implements StreamObserver<TwinResponseProto> {

        private final StreamObserver<CloudToMinionMessage> delegate;

        AdapterObserver(StreamObserver<CloudToMinionMessage> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onNext(TwinResponseProto value) {
            delegate.onNext(map(value));
        }

        @Override
        public void onError(Throwable t) {

        }

        @Override
        public void onCompleted() {

        }

        private CloudToMinionMessage map(TwinResponseProto value) {
            return CloudToMinionMessage.newBuilder()
                .setTwinResponse(value)
                .build();
        }
    }

    // BiConsumer<MinionHeader, StreamObserver<CloudToMinionMessage>>
    public BiConsumer<MinionHeader, StreamObserver<CloudToMinionMessage>> getStreamObserver() {
        return new BiConsumer<>() {
            @Override
            public void accept(MinionHeader minionHeader, StreamObserver<CloudToMinionMessage> responseObserver) {
                if (sinkStreamsBySystemId.containsKey(minionHeader.getSystemId())) {
                    StreamObserver<TwinResponseProto> sinkStream = sinkStreamsBySystemId.remove(minionHeader.getSystemId());
                    sinkStreamsByLocation.remove(minionHeader.getLocation(), sinkStream);
                }
                AdapterObserver delegate = new AdapterObserver(responseObserver);
                sinkStreamsByLocation.put(minionHeader.getLocation(), delegate);
                sinkStreamsBySystemId.put(minionHeader.getSystemId(), delegate);

                forEachSession(((sessionKey, twinTracker) -> {
                    if (sessionKey.location == null || sessionKey.location.equals(minionHeader.getLocation())) {
                        TwinUpdate twinUpdate = new TwinUpdate(sessionKey.key, sessionKey.location, twinTracker.getObj());
                        twinUpdate.setSessionId(twinTracker.getSessionId());
                        twinUpdate.setVersion(twinTracker.getVersion());
                        twinUpdate.setPatch(false);
                        TwinResponseProto twinResponseProto = mapTwinResponse(twinUpdate);
                        delegate.onNext(twinResponseProto);
                    }
                }));
            }
        };
    }

    // BiConsumer<RpcRequestProto, StreamObserver<RpcResponseProto>>
    public BiConsumer<RpcRequestProto, StreamObserver<RpcResponseProto>> getRpcObserver() {
        return new BiConsumer<RpcRequestProto, StreamObserver<RpcResponseProto>>() {
            @Override
            public void accept(RpcRequestProto request, StreamObserver<RpcResponseProto> responseObserver) {
                if (request.getModuleId().equals("twin")) {
                    try {
                        CompletableFuture.runAsync(() -> {
                            try {
                                TwinRequest twinRequest = mapTwinRequestProto(request.getRpcContent().toByteArray());
                                TwinUpdate twinUpdate = getTwin(twinRequest);
                                TwinResponseProto twinResponseProto = mapTwinResponse(twinUpdate);
                                LOG.debug("Sent Twin response for key {} at location {}", twinRequest.getKey(), twinRequest.getLocation());
                                RpcResponseProto rpcResponse = RpcResponseProto.newBuilder()
                                    .setModuleId("twin")
                                    .setRpcId(request.getRpcId())
                                    .setSystemId(request.getSystemId())
                                    .setLocation(request.getLocation())
                                    .setRpcContent(twinResponseProto.toByteString())
                                    .build();
                                responseObserver.onNext(rpcResponse);
                            } catch (Exception e) {
                                LOG.error("Exception while processing request", e);
                            }
                        }, twinRpcExecutor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

}
