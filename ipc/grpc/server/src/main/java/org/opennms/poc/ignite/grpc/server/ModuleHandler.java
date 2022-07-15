package org.opennms.poc.ignite.grpc.server;

import java.util.concurrent.CompletableFuture;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;

public interface ModuleHandler {

  void start(GrpcServer server);

  interface Module {
    String getId();

    void start(GrpcServer server);
  }

  interface IncomingRpcModule<Local extends RpcRequest, Remote extends RpcResponse> extends Module {
    CompletableFuture<Remote> handle(Local request);
    Class<Local> receive();
    Class<Remote> answer();
  }

  interface OutgoingRpcModule<Local extends RpcRequest, Remote extends RpcResponse> extends Module {
    Class<Local> sending();
    Class<Remote> expecting();
  }

  interface CollectorModule<MsgIn> extends Module {
    Class<MsgIn> payload();

    void handle(MsgIn message);
  }

  interface PushModule<MsgIn> extends Module {
    Class<MsgIn> payload();
  }

  <In> void registerCollector(CollectorModule<In> module);
  <In> void unregisterCollector(CollectorModule<In> module);

  <Out> void registerPush(PushModule<Out> module);
  <Out> void unregisterPush(PushModule<Out> module);

  <Local extends RpcRequest, Remote extends RpcResponse> void registerIncomingRpc(IncomingRpcModule<Local, Remote> module);
  <Local extends RpcRequest, Remote extends RpcResponse> void unregisterIncomingRpc(IncomingRpcModule<Local, Remote> module);

  <Local extends RpcRequest, Remote extends RpcResponse> void registerOutgoingRpc(OutgoingRpcModule<Local, Remote> module);
  <Local extends RpcRequest, Remote extends RpcResponse> void unregisterOutgoingRpc(OutgoingRpcModule<Local, Remote> module);

}
