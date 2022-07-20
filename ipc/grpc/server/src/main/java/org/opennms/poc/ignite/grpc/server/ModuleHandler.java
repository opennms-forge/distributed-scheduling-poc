package org.opennms.poc.ignite.grpc.server;

import com.google.protobuf.Message;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;

public interface ModuleHandler {

  void start(GrpcServer server);

  CompletableFuture<ZonedDateTime> push(String systemId, String location, CloudToMinionMessage message);
  CompletableFuture<RpcResponseProto> request(String systemId, String location, RpcRequestProto request);

  interface Module {
    String getId();

    void start(GrpcServer server);
  }

  interface IncomingRpcModule<Local extends Message, Remote extends Message> extends Module {
    CompletableFuture<Remote> handle(Local request);
    Predicate<RpcRequestProto> predicate();
    Class<Local> receive();
  }

  interface OutgoingRpcModule extends Module {
  }

  interface CollectorModule<MsgIn extends Message> extends Module {
    Predicate<MsgIn> predicate();

    void handle(MsgIn message);
  }

  interface PushModule<MsgIn extends Message> extends Module {
  }

  <In extends Message> void registerCollector(CollectorModule<In> module);
  <In extends Message> void unregisterCollector(CollectorModule<In> module);

  <Out extends Message> void registerPush(PushModule<Out> module);
  <Out extends Message> void unregisterPush(PushModule<Out> module);

  <Local extends Message, Remote extends Message> void registerIncomingRpc(IncomingRpcModule<Local, Remote> module);
  <Local extends Message, Remote extends Message> void unregisterIncomingRpc(IncomingRpcModule<Local, Remote> module);

  void registerOutgoingRpc(OutgoingRpcModule module);
  void unregisterOutgoingRpc(OutgoingRpcModule module);

}
