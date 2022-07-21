package org.opennms.poc.testdriver.config;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.stub.StreamObserver;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.cloud.grpc.minion.TwinRequestProto;
import org.opennms.cloud.grpc.minion.TwinResponseProto;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.ipc.grpc.server.OpennmsGrpcServer;
import org.opennms.core.ipc.grpc.server.manager.LocationIndependentRpcClientFactory;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.core.ipc.grpc.server.manager.RpcConnectionTracker;
import org.opennms.core.ipc.grpc.server.manager.RpcRequestTracker;
import org.opennms.core.ipc.grpc.server.manager.impl.MinionManagerImpl;
import org.opennms.core.ipc.grpc.server.manager.impl.RpcConnectionTrackerImpl;
import org.opennms.core.ipc.grpc.server.manager.impl.RpcRequestTrackerImpl;
import org.opennms.core.ipc.grpc.server.manager.rpc.LocationIndependentRpcClientFactoryImpl;
import org.opennms.core.ipc.grpc.server.manager.rpcstreaming.MinionRpcStreamConnectionManager;
import org.opennms.core.ipc.grpc.server.manager.rpcstreaming.impl.MinionRpcStreamConnectionManagerImpl;
import org.osgi.service.component.annotations.Reference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    @Bean
    public OpennmsGrpcServer opennmsServer(@Reference GrpcIpcServer serverBuilder) throws Exception {
        OpennmsGrpcServer server = new OpennmsGrpcServer(serverBuilder);

        RpcConnectionTracker rpcConnectionTracker = new RpcConnectionTrackerImpl();
        RpcRequestTracker rpcRequestTracker = new RpcRequestTrackerImpl();
        MinionManager minionManager = new MinionManagerImpl();
        ExecutorService responseHandlerExecutor = Executors.newSingleThreadExecutor();
        LocationIndependentRpcClientFactory locationIndependentRpcClientFactory = new LocationIndependentRpcClientFactoryImpl();

        server.setRpcConnectionTracker(rpcConnectionTracker);
        server.setRpcRequestTracker(rpcRequestTracker);
        server.setMinionManager(minionManager);
        server.setLocationIndependentRpcClientFactory(locationIndependentRpcClientFactory);
        MinionRpcStreamConnectionManager manager = new MinionRpcStreamConnectionManagerImpl(
            rpcConnectionTracker, rpcRequestTracker, minionManager, responseHandlerExecutor
        );
        server.setMinionRpcStreamConnectionManager(manager);

        server.setIncomingRpcHandler(new BiConsumer<RpcRequestProto, StreamObserver<RpcResponseProto>>() {
            @Override
            public void accept(RpcRequestProto request, StreamObserver<RpcResponseProto> responseStream) {
                if (request.getModuleId().equals("twin")) {
                  try {
                      // todo use requested keys
                      TwinRequestProto requestProto = TwinRequestProto.parseFrom(request.getRpcContent());
                      TwinResponseProto twinResponse = createTwinResponse(false);

                      RpcResponseProto response = RpcResponseProto.newBuilder()
                          .setModuleId("twin")
                          .setRpcId(request.getRpcId())
                          .setRpcContent(twinResponse.toByteString())
                          .build();
                      responseStream.onNext(response);
                  } catch (InvalidProtocolBufferException e) {
                      e.printStackTrace();
                  }
                }
            }
        });
        server.setOutgoingMessageHandler(new BiConsumer<MinionHeader, StreamObserver<CloudToMinionMessage>>() {
            @Override
            public void accept(MinionHeader minionHeader, StreamObserver<CloudToMinionMessage> responseStream) {
                responseStream.onNext(
                    CloudToMinionMessage.newBuilder()
                        .setTwinResponse(createTwinResponse(true))
                        .build()
                );
            }
        });

        server.start();
        return server;
    }

    private TwinResponseProto createTwinResponse(boolean patch) {
        TwinResponseProto twinResponse = TwinResponseProto.newBuilder()
            .setConsumerKey("workflow")
            .setIsPatchObject(patch)
            .setSystemId("minion01")
            .setSessionId(UUID.randomUUID().toString())
            .setLocation("dc1")
            .setTwinObject(ByteString.copyFromUtf8("{\"workflows\": []}"))
            .build();
        return twinResponse;
    }

}
