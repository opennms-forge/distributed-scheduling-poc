package org.opennms.poc.ignite.grpc.server.internal;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.poc.ignite.grpc.server.GrpcServer;
import org.opennms.poc.ignite.grpc.server.ModuleHandler;

public class BasicGrpcServer implements GrpcServer {

  private final ServerBuilder<?> builder;
  private Server server;
  private ModuleHandler handler;

  public BasicGrpcServer(ServerBuilder<?> builder) {
    this.builder = builder;
  }

  @Override
  public void register(ModuleHandler handler) {
    this.handler = handler;
  }

  @Override
  public void start() throws IOException {
    if (handler instanceof BindableService) {
      builder.addService((BindableService) handler);
    }
    server = builder.build();
    server.start();

    handler.start(this);
  }

  @Override
  public CompletableFuture<ZonedDateTime> push(String systemId, String location, CloudToMinionMessage message) {
    return handler.push(systemId, location, message);
  }

  @Override
  public CompletableFuture<Void> broadcast(CloudToMinionMessage message) {
    // force "void" result
    return handler.push(null, null, message)
      .thenApply(result -> null);
  }

  @Override
  public CompletableFuture<RpcResponse> request(String systemId, String location, RpcRequest request) {
    return handler.request(systemId, location, request);
  }

  @Override
  public void stop() {
    if (server != null) {
      server.shutdown();
    }
  }
}
