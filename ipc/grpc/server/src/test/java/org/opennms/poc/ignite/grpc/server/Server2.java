package org.opennms.poc.ignite.grpc.server;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
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

public class Server2 {
  public static void main(String[] args) throws Exception {
    Properties properties = new Properties();

    GrpcIpcServerBuilder serverBuilder = new GrpcIpcServerBuilder(properties, 8990, "PT5S");
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



    server.start();
  }
}
