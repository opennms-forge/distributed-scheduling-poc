package org.opennms.poc.ignite.grpc.client.internal;

import java.util.LinkedHashSet;
import java.util.Set;
import org.opennms.horizon.core.identity.Identity;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.MinionClient;

public class SimpleMinionClient implements MinionClient {

  private final GrpcClient client;
  private final Identity identity;
  private Set<PublishModule> publishers = new LinkedHashSet<>();
  private Set<SinkModule> subscribers = new LinkedHashSet<>();
  private Set<IncomingRpcModule> incoming = new LinkedHashSet<>();
  private Set<OutgoingRpcModule> outgoing = new LinkedHashSet<>();

  public SimpleMinionClient(GrpcClient client, Identity identity) {
    this.client = client;
    this.identity = identity;
  }

  public void start() {
    client.start();
    publishers.forEach(module -> module.start(client));
    subscribers.forEach(module -> module.start(client));
    incoming.forEach(module -> module.start(client));
    outgoing.forEach(module -> module.start(client));
  }

  @Override
  public void register(PublishModule module) {
    publishers.add(module);
  }

  @Override
  public void unregister(PublishModule module) {
    publishers.remove(module);
  }

  @Override
  public void register(SinkModule module) {
    subscribers.add(module);
  }

  @Override
  public void unregister(SinkModule module) {
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
