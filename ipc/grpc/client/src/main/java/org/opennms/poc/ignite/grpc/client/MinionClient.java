package org.opennms.poc.ignite.grpc.client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MinionClient {

  <Out> void register(PublishModule<Out> module);
  <Out> void unregister(PublishModule<Out> module);

  <In> void register(SinkModule<In> module);
  <In> void unregister(SinkModule<In> module);

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

  interface SinkModule<MsgIn> extends Module {
    Class<MsgIn> payload();

    void handle(MsgIn request);
  }

  interface PublishModule<Msg> extends Module {
    Class<Msg> payload();
  }

}
