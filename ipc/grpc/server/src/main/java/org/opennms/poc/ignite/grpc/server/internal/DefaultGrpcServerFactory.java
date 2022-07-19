package org.opennms.poc.ignite.grpc.server.internal;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.ServerBuilder;
import io.grpc.ServerCredentials;
import io.grpc.ServerInterceptor;
import java.util.List;
import org.opennms.poc.ignite.grpc.server.GrpcServer;
import org.opennms.poc.ignite.grpc.server.GrpcServerFactory;

public class DefaultGrpcServerFactory implements GrpcServerFactory {

  @Override
  public GrpcServer create(String host, int port, List<ServerInterceptor> interceptors) {
    ServerCredentials credentials = InsecureServerCredentials.create();

    ServerBuilder<?> serverBuilder = Grpc.newServerBuilderForPort(port, credentials)
      .intercept(new DelegatingInterceptor(interceptors));

    return new BasicGrpcServer(serverBuilder);
  }
}
