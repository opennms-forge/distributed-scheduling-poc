package org.opennms.poc.testdriver.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

        server.start();
        return server;
    }


}
