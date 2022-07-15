package org.opennms.poc.ignite.grpc.server;

import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.poc.ignite.grpc.server.ModuleHandler.IncomingRpcModule;
import org.opennms.poc.ignite.grpc.server.internal.DefaultGrpcServerBuilder;
import org.opennms.poc.ignite.grpc.server.internal.StaticModuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {

  public static void main(String[] args) throws IOException {
    DefaultGrpcServerBuilder serverBuilder = new DefaultGrpcServerBuilder();
    GrpcServer server = serverBuilder.build("anything", 8080);
    StaticModuleHandler moduleHandler = new StaticModuleHandler();
    moduleHandler.registerIncomingRpc(new RpcRegistrationModule());
    server.register(moduleHandler);
    server.start();
    System.in.read();
    server.stop();
  }

  static class RpcRegistrationModule implements IncomingRpcModule<RpcRequest, RpcResponse> {

    public final static String MODULE_ID = "registration";

    private final Logger logger = LoggerFactory.getLogger(RpcRegistrationModule.class);
    private GrpcServer server;

    @Override
    public String getId() {
      return MODULE_ID;
    }

    @Override
    public void start(GrpcServer server) {
      this.server = server;
    }

    @Override
    public CompletableFuture<RpcResponse> handle(RpcRequest request) {
      logger.info("Received request {}", request);
      return CompletableFuture.completedFuture(RpcResponse.newBuilder()
        .setRpcContent(ByteString.copyFromUtf8("hello"))
        .build()
      );
    }

    @Override
    public Class<RpcRequest> receive() {
      return RpcRequest.class;
    }

    @Override
    public Class<RpcResponse> answer() {
      return RpcResponse.class;
    }
  }

}
