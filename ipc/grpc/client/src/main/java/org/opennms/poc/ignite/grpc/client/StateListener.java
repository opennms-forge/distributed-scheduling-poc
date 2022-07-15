package org.opennms.poc.ignite.grpc.client;

public interface StateListener {

  void connected(GrpcClient client);
  void disconnected(GrpcClient client);

}
