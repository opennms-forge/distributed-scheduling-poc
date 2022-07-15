package org.opennms.poc.ignite.grpc.server.internal;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.opennms.poc.ignite.grpc.server.GrpcServer;
import org.opennms.poc.ignite.grpc.server.ModuleHandler;

public class BasicGrpcServer implements GrpcServer {

  private final Set<BindableService> services = new LinkedHashSet<>();
  private final ServerBuilder<?> builder;
  private Server server;

  public BasicGrpcServer(ServerBuilder<?> builder) {
    this.builder = builder;
  }

  @Override
  public void register(BindableService service) {
    builder.addService(service);
  }

  @Override
  public void start() throws IOException {
    server = builder.build();
    server.start();

    services.forEach(service -> {
      if (service instanceof ModuleHandler) {
        ((ModuleHandler) service).start(this);
      }
    });
  }

  @Override
  public void stop() {
    if (server != null) {
      server.shutdown();
    }
  }
}
