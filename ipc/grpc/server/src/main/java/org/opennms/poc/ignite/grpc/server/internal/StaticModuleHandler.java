package org.opennms.poc.ignite.grpc.server.internal;

import io.grpc.stub.StreamObserver;
import java.util.LinkedHashSet;
import java.util.Set;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceImplBase;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.Empty;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.poc.ignite.grpc.server.GrpcServer;
import org.opennms.poc.ignite.grpc.server.ModuleHandler;

public class StaticModuleHandler extends CloudServiceImplBase implements ModuleHandler {

  private final Set<CollectorModule> collectors = new LinkedHashSet<>();
  private final Set<PushModule> pushers = new LinkedHashSet<>();
  private final Set<IncomingRpcModule<RpcRequest, RpcResponse>> incoming = new LinkedHashSet<>();
  private final Set<OutgoingRpcModule<RpcRequest, RpcResponse>> outgoing = new LinkedHashSet<>();

  @Override
  public void start(GrpcServer server) {
    collectors.forEach(module -> module.start(server));
    pushers.forEach(module -> module.start(server));
    incoming.forEach(module -> module.start(server));
    outgoing.forEach(module -> module.start(server));
  }

  @Override
  public StreamObserver<RpcResponse> cloudToMinionRPC(StreamObserver<RpcRequest> responseObserver) {
    super.cloudToMinionRPC(responseObserver);
    return null;
  }

  @Override
  public void cloudToMinionMessages(MinionHeader request, StreamObserver<CloudToMinionMessage> responseObserver) {
    super.cloudToMinionMessages(request, responseObserver);
  }

  @Override
  public void minionToCloudRPC(RpcRequest request, StreamObserver<RpcResponse> responseObserver) {
    for (IncomingRpcModule<RpcRequest, RpcResponse> module : incoming) {
      Class<RpcRequest> receive = module.receive();
      if (receive.isInstance(request)) {
        module.handle(receive.cast(request)).whenComplete((response, error) -> {
          if (error != null) {
            responseObserver.onError(error);
            return;
          }
          responseObserver.onNext(response);
        });
      }
    }
  }

  @Override
  public StreamObserver<MinionToCloudMessage> minionToCloudMessages(StreamObserver<Empty> responseObserver) {
    return new StreamObserver<MinionToCloudMessage>() {
      @Override
      public void onNext(MinionToCloudMessage value) {
        for (CollectorModule module : collectors) {
          Class payload = module.payload();
          if (payload.isInstance(value)) {
            module.handle(payload.cast(value));
          }
        }
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {

      }
    };
  }


  @Override
  public <In> void registerCollector(CollectorModule<In> module) {
    this.collectors.add(module);
  }

  @Override
  public <In> void unregisterCollector(CollectorModule<In> module) {
    this.collectors.remove(module);
  }

  @Override
  public <Out> void registerPush(PushModule<Out> module) {
    this.pushers.add(module);
  }

  @Override
  public <Out> void unregisterPush(PushModule<Out> module) {
    this.pushers.remove(module);
  }

  @Override
  public <Local extends RpcRequest, Remote extends RpcResponse> void registerIncomingRpc(IncomingRpcModule<Local, Remote> module) {
    incoming.add((IncomingRpcModule<RpcRequest, RpcResponse>) module);
  }

  @Override
  public <Local extends RpcRequest, Remote extends RpcResponse> void unregisterIncomingRpc(IncomingRpcModule<Local, Remote> module) {
    incoming.remove(module);
  }

  @Override
  public <Local extends RpcRequest, Remote extends RpcResponse> void registerOutgoingRpc(OutgoingRpcModule<Local, Remote> module) {
    outgoing.add((OutgoingRpcModule<RpcRequest, RpcResponse>) module);
  }

  @Override
  public <Local extends RpcRequest, Remote extends RpcResponse> void unregisterOutgoingRpc(OutgoingRpcModule<Local, Remote> module) {
    outgoing.remove(module);
  }
}
