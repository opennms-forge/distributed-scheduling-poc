package org.opennms.poc.testdriver.config;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

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
import org.opennms.core.ipc.twin.grpc.publisher.GrpcTwinPublisher;
import org.opennms.horizon.ipc.sink.api.MessageConsumer;
import org.opennms.horizon.ipc.sink.api.SinkModule;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults.WorkflowResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import orh.opennms.poc.ignite.grpc.workflow.WorkflowSinkModule;

@Configuration
public class GrpcServerConfig {

    @Bean
    public OpennmsGrpcServer opennmsServer(GrpcIpcServer serverBuilder, GrpcTwinPublisher publisher) throws Exception {
        OpennmsGrpcServer server = new OpennmsGrpcServer(serverBuilder);

        RpcConnectionTracker rpcConnectionTracker = new RpcConnectionTrackerImpl();
        RpcRequestTracker rpcRequestTracker = new RpcRequestTrackerImpl();
        MinionManager minionManager = new MinionManagerImpl();
        ScheduledExecutorService responseHandlerExecutor = Executors.newSingleThreadScheduledExecutor();
        LocationIndependentRpcClientFactory locationIndependentRpcClientFactory = new LocationIndependentRpcClientFactoryImpl();

        server.setRpcConnectionTracker(rpcConnectionTracker);
        server.setRpcRequestTracker(rpcRequestTracker);
        server.setMinionManager(minionManager);
        server.setLocationIndependentRpcClientFactory(locationIndependentRpcClientFactory);
        MinionRpcStreamConnectionManager manager = new MinionRpcStreamConnectionManagerImpl(
            rpcConnectionTracker, rpcRequestTracker, minionManager, responseHandlerExecutor
        );
        server.setMinionRpcStreamConnectionManager(manager);
        server.setIncomingRpcHandler(publisher.getRpcObserver());
        server.setOutgoingMessageHandler(publisher.getStreamObserver());

        WorkflowSinkModule workflowSinkModule = new WorkflowSinkModule();
        server.registerConsumer(new MessageConsumer<WorkflowResults, WorkflowResults>() {
            private AtomicLong samplesCounted = new AtomicLong(0);

            @Override
            public SinkModule<WorkflowResults, WorkflowResults> getModule() {
                return workflowSinkModule;
            }

            @Override
            public void handleMessage(WorkflowResults message) {
                for (WorkflowResult result : message.getResultsList()) {
                    Map<String, Object> resultMap = new LinkedHashMap<>();
                    for (Entry<String, Any> entry : result.getParametersMap().entrySet()) {
                        try {
                            Value value = entry.getValue().unpack(Value.class);
                            if (value.hasNullValue()) {
                                resultMap.put(entry.getKey(), null);
                            } else if (value.hasStringValue()) {
                                resultMap.put(entry.getKey(), value.getStringValue());
                            } else if (value.hasNumberValue()) {
                                resultMap.put(entry.getKey(), value.getNumberValue());
                            } else {
                                // unsupported value
                            }
                        } catch (InvalidProtocolBufferException e) {
                            // FIXME - log an error
                            e.printStackTrace();
                        }
                    }

                    long sampleCount = samplesCounted.incrementAndGet();
                    if (sampleCount < 20) {
                        System.out.println("Sample count received " + sampleCount);
                        System.out.println("Received result workflow=" + result.getUuid() + " status=" + result.getStatus() + " result=" + result.getReason() + " params=" + resultMap);
                    } else if ((sampleCount < 1000) && ((sampleCount % 100) == 0)) {
                        System.out.println("..Sample count received " + sampleCount);
                    } else if ((sampleCount < 10000) && ((sampleCount % 1000) == 0)) {
                        System.out.println("...Sample count received " + sampleCount);
                    } else if ((sampleCount % 10000) == 0) {
                        System.out.println("....Sample count received " + sampleCount);
                    }
                }
            }
        });

        server.start();
        return server;
    }


}
