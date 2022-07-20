package org.opennms.poc.ignite.grpc.client;

import java.util.Properties;
import org.opennms.core.ipc.grpc.client.GrpcClientConstants;
import org.opennms.core.ipc.grpc.client.MinionGrpcClient;
import org.opennms.horizon.core.identity.IdentityImpl;

public class Client2 {
  public static void main(String[] args) throws Exception {
    IdentityImpl identity = new IdentityImpl("minion01", "dc1", "minion");
    Properties properties = new Properties();
    properties.put(GrpcClientConstants.GRPC_PORT, 8080);
    MinionGrpcClient client = new MinionGrpcClient(identity, properties);
    client.start();

    System.in.read();
  }
}
