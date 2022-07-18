package org.opennms.poc.ignite.grpc.client;

import java.util.concurrent.CompletableFuture;

public interface MinionClient {

  void register(PublishModule module);
  void unregister(PublishModule module);

  void register(SinkModule module);
  void unregister(SinkModule module);

  <Req, Res> void registerIncomingRpc(IncomingRpcModule<Req, Res> module);
  <Req, Res> void unregisterIncomingRpc(IncomingRpcModule<Req, Res> module);

  <Out, Ack> void registerOutgoingRpc(OutgoingRpcModule<Out, Ack> module);
  <Out, Ack> void unregisterOutgoingRpc(OutgoingRpcModule<Out, Ack> module);

  interface Module {
    String getId();

    void start(GrpcClient client);
    //void attached(UUID sessionId);
  }

  interface IncomingRpcModule<Req, Res> extends Module {
    CompletableFuture<Res> handle(Req request);
  }

  interface OutgoingRpcModule<Out, Ack> extends Module {
    Class<Out> payload();
    Class<Ack> ack();
  }

  interface SinkModule extends Module {
  }

  interface PublishModule extends Module {
  }

}
