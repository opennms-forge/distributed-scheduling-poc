package org.opennms.poc.ignite.grpc.client.internal;

import java.util.LinkedHashSet;
import java.util.Set;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.MinionClient;

public class SimpleMinionClient implements MinionClient {

  private final GrpcClient client;
  private Set<PublishModule> publishers = new LinkedHashSet<>();
  private Set<SinkModule> subscribers = new LinkedHashSet<>();
  private Set<IncomingRpcModule> incoming = new LinkedHashSet<>();
  private Set<OutgoingRpcModule> outgoing = new LinkedHashSet<>();

  public SimpleMinionClient(GrpcClient client) {
    this.client = client;
  }

  public void start() {
    client.start();
    publishers.forEach(module -> module.start(client));
    subscribers.forEach(module -> module.start(client));
    incoming.forEach(module -> module.start(client));
    outgoing.forEach(module -> module.start(client));
  }

  @Override
  public <Out> void register(PublishModule<Out> module) {
    publishers.add(module);
  }

  @Override
  public <Out> void unregister(PublishModule<Out> module) {
    publishers.remove(module);
  }

  @Override
  public <In> void register(SinkModule<In> module) {
    subscribers.add(module);
  }

  @Override
  public <In> void unregister(SinkModule<In> module) {
    subscribers.remove(module);
  }

  @Override
  public <Req, Res> void registerIncomingRpc(IncomingRpcModule<Req, Res> module) {
    incoming.add(module);
  }

  @Override
  public <Req, Res> void unregisterIncomingRpc(IncomingRpcModule<Req, Res> module) {
    incoming.remove(module);
  }

  @Override
  public <Out, Ack> void registerOutgoingRpc(OutgoingRpcModule<Out, Ack> module) {
    outgoing.add(module);
  }

  @Override
  public <Out, Ack> void unregisterOutgoingRpc(OutgoingRpcModule<Out, Ack> module) {
    outgoing.remove(module);
  }

}
