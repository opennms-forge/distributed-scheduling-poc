package org.opennms.poc.ignite.grpc.client.internal;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceBlockingStub;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceStub;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.Empty;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.horizon.core.identity.Identity;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubGrpcClient implements GrpcClient {

  private final Logger logger = LoggerFactory.getLogger(StubGrpcClient.class);

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "grpc-client-call"));
  private final ManagedChannel channel;
  private final CloudServiceStub asnc;
  private final CloudServiceBlockingStub blocking;
  private final Map<Predicate<RpcRequest>, Function<RpcRequest, CompletableFuture<RpcResponse>>> handlers = new ConcurrentHashMap<>();
  private final Identity identity;
  private StreamObserver<RpcResponse> rpcReplyStream;

  public StubGrpcClient(ManagedChannel channel, CloudServiceStub async, CloudServiceBlockingStub blocking, Identity identity) {
    this.channel = channel;
    this.asnc = async;
    this.blocking = blocking;
    this.identity = identity;
  }

  @Override
  public void start() {
    channel.getState(true);

    this.rpcReplyStream = asnc.cloudToMinionRPC(new StreamObserver<>() {
      @Override
      public void onNext(RpcRequest value) {
        if (value.getModuleId().equals("identify")) {
          RpcResponse response = RpcResponse.newBuilder()
            .setSystemId(identity.getId())
            .setLocation(identity.getLocation())
            .setRpcId(value.getRpcId())
            .setModuleId(value.getModuleId())
            .build();
          executor.schedule(new Runnable() {
            @Override
            public void run() {
              rpcReplyStream.onNext(response);
            }
          }, 2, TimeUnit.SECONDS);
          return;
        }

        handleIncomingRpc(value);
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {

      }
    });
  }

  @Override
  public void close() throws Exception {
    if (getState() != ConnectivityState.SHUTDOWN) {
      channel.shutdown();
    }
  }

  @Override
  public ConnectivityState getState() {
    return channel.getState(false);
  }

  public Session onCall(Predicate<RpcRequest> predicate, Function<RpcRequest, CompletableFuture<RpcResponse>> function) {
    this.handlers.put(predicate, function);
    return new Session() {
      @Override
      public void close() throws IOException {
        handlers.remove(predicate);
      }
    };
  }

  public PublishSession publishToCloud() {
    StreamObserver<MinionToCloudMessage> observer = asnc.minionToCloudMessages(new EmptyObserver());
    return new PublishSession() {
      @Override
      public void close() throws IOException {
        observer.onCompleted();
      }

      @Override
      public void publish(MinionToCloudMessage message) {
        observer.onNext(message);
      }
    };
  }

  public Session streamFromCloud(Consumer<CloudToMinionMessage> consumer) {
    MinionHeader header = MinionHeader.newBuilder()
      .setLocation(identity.getLocation())
      .setSystemId(identity.getId())
      .build();

    StreamObserver<CloudToMinionMessage> observer = new ForwardingObserver<>(new Consumer<>() {
        @Override
        public void accept(CloudToMinionMessage cloudToMinionMessage) {
          consumer.accept(cloudToMinionMessage);
        }
      });
    asnc.cloudToMinionMessages(header, observer);
    return new Session() {
      @Override
      public void close() throws IOException {
        // do we close it ?!
        observer.onCompleted();
      }
    };
  }

  @Override
  public RpcResponse request(RpcRequest request) {
    return blocking.minionToCloudRPC(request);
  }

  private void handleIncomingRpc(RpcRequest request) {
    for (Entry<Predicate<RpcRequest>, Function<RpcRequest, CompletableFuture<RpcResponse>>> entry : handlers.entrySet()) {
      if (entry.getKey().test(request)) {
        entry.getValue().apply(request).whenComplete((response, error) -> {
          if (error != null) {
            logger.warn("Client side error while handling rpc request {}", request, error);
            return;
          }
          rpcReplyStream.onNext(response);
          rpcReplyStream.onCompleted();
        });
      }
    }
  }

  static class EmptyObserver implements StreamObserver<Empty> {

    @Override
    public void onNext(Empty value) {

    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onCompleted() {

    }
  }

}
