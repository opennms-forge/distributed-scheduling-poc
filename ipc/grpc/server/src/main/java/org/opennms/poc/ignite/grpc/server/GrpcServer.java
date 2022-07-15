package org.opennms.poc.ignite.grpc.server;

import io.grpc.BindableService;
import java.io.IOException;

public interface GrpcServer {

  // one way for now
  void register(BindableService service);

  void start() throws IOException;
  void stop();

}
