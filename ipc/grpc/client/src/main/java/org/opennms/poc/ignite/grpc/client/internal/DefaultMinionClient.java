package org.opennms.poc.ignite.grpc.client.internal;

import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.MinionClient;

public class DefaultMinionClient implements MinionClient {

  private final GrpcClient client;

  public DefaultMinionClient(GrpcClient client) {

    this.client = client;
  }

  @Override
  public <Out> void register(PublishModule<Out> module) {

  }

  @Override
  public <Out> void unregister(PublishModule<Out> module) {

  }

  @Override
  public <In> void register(SinkModule<In> module) {

  }

  @Override
  public <In> void unregister(SinkModule<In> module) {

  }

  @Override
  public <Req, Res> void registerIncomingRpc(IncomingRpcModule<Req, Res> module) {

  }

  @Override
  public <Req, Res> void unregisterIncomingRpc(IncomingRpcModule<Req, Res> module) {

  }

  @Override
  public <Out, Ack> void registerOutgoingRpc(OutgoingRpcModule<Out, Ack> module) {

  }

  @Override
  public <Out, Ack> void unregisterOutgoingRpc(OutgoingRpcModule<Out, Ack> module) {

  }
}
