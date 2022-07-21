package org.opennms.poc.testdriver.config;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
import org.opennms.core.ipc.twin.api.TwinPublisher.Session;
import org.opennms.core.ipc.twin.grpc.publisher.GrpcTwinPublisher;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.model.workflows.Workflows;
import org.osgi.service.component.annotations.Reference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        // fixme - remove once proved that thing actually works!
        responseHandlerExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Session<Workflows> session = publisher.register("workflow", Workflows.class, "dc1");
                    Workflows workflows = new Workflows();
                    Workflow workflow = new Workflow();
                    workflow.setUuid(UUID.randomUUID().toString());
                    workflows.setWorkflows(Arrays.asList(workflow));
                    session.publish(workflows);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 15, 30, TimeUnit.SECONDS);

        server.start();
        return server;
    }


}
