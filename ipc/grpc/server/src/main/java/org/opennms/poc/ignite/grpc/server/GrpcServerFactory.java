package org.opennms.poc.ignite.grpc.server;

import io.grpc.ServerInterceptor;
import java.util.Collections;
import java.util.List;

public interface GrpcServerFactory {

  default GrpcServer create(String host, int port) {
    return create(host, port, Collections.emptyList());
  }

  GrpcServer create(String host, int port, List<ServerInterceptor> interceptors);
}
