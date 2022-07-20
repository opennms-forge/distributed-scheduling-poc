package org.opennms.poc.ignite.grpc.client;

import java.util.Arrays;
import org.opennms.horizon.core.identity.IdentityImpl;
import org.opennms.poc.ignite.grpc.client.internal.DefaultGrpcClientFactory;
import org.opennms.poc.ignite.grpc.client.internal.SimpleMinionClient;
import org.opennms.poc.ignite.grpc.client.module.IncomingEchoRpcModule;
import org.opennms.poc.ignite.grpc.client.module.MinionPushModule;
import org.opennms.poc.ignite.grpc.client.module.MinionReceiveModule;
import org.opennms.poc.ignite.grpc.client.module.OutgoingRegistrationRpcModule;

public class MinionTestClient {

  public static void main(String[] args) throws Exception {
    IdentityImpl identity = new IdentityImpl("minion01", "dc1", "minion");
    DefaultGrpcClientFactory clientFactory = new DefaultGrpcClientFactory(identity);
    GrpcClient grpcClient = clientFactory.create(
      "localhost", 8990, Arrays.asList()
    );
    grpcClient.start();

    SimpleMinionClient client = new SimpleMinionClient(grpcClient, identity);
    client.registerIncomingRpc(new IncomingEchoRpcModule());
    client.registerOutgoingRpc(new OutgoingRegistrationRpcModule());
    client.register(new MinionPushModule());
    client.register(new MinionReceiveModule());
    client.start();

    System.in.read();
  }

}
