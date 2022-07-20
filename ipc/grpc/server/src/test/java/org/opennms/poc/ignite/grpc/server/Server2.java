package org.opennms.poc.ignite.grpc.server;

import io.grpc.BindableService;
import java.util.Properties;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
import org.opennms.core.ipc.grpc.server.manager.adapter.MinionRSTransportAdapter;

public class Server2 {
  public static void main(String[] args) throws Exception {
    Properties properties = new Properties();
    BindableService service = new MinionRSTransportAdapter(null);
    new GrpcIpcServerBuilder(properties, 8080, "PT10S")
      .startServer(service);
  }
}
