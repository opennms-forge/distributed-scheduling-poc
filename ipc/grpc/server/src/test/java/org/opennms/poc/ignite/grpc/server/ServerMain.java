package org.opennms.poc.ignite.grpc.server;

import java.io.IOException;
import org.opennms.poc.ignite.grpc.server.internal.DefaultGrpcServerBuilder;
import org.opennms.poc.ignite.grpc.server.internal.StaticModuleHandler;
import org.opennms.poc.ignite.grpc.server.module.EchoRequestModule;
import org.opennms.poc.ignite.grpc.server.module.RpcRegistrationModule;
import org.opennms.poc.ignite.grpc.server.module.SamplePushMessageModule;
import org.opennms.poc.ignite.grpc.server.module.SinkMessageCollectorModule;

public class ServerMain {

  public static void main(String[] args) throws IOException {
    DefaultGrpcServerBuilder serverBuilder = new DefaultGrpcServerBuilder();
    GrpcServer server = serverBuilder.build("anything", 8080);
    StaticModuleHandler moduleHandler = new StaticModuleHandler();
    moduleHandler.registerOutgoingRpc(new EchoRequestModule());
    moduleHandler.registerIncomingRpc(new RpcRegistrationModule());
    moduleHandler.registerCollector(new SinkMessageCollectorModule());
    moduleHandler.registerPush(new SamplePushMessageModule());
    server.register(moduleHandler);
    server.start();
    System.in.read();
    server.stop();
  }

}
