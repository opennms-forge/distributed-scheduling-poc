package org.opennms.poc.ignite.grpc.server;

import io.grpc.ServerInterceptor;
import java.util.Collections;
import java.util.List;

public interface GrpcServerBuilder {

  default GrpcServer build(String host, int port) {
    return build(host, port, Collections.emptyList());
  }

  GrpcServer build(String host, int port, List<ServerInterceptor> interceptors);
}
