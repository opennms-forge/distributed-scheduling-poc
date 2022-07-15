package org.opennms.poc.ignite.grpc.client.internal;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceBlockingStub;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceStub;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.Empty;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.horizon.core.identity.Identity;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubGrpcClient implements GrpcClient {

  private final Logger logger = LoggerFactory.getLogger(StubGrpcClient.class);

  private final ManagedChannel channel;
  private final CloudServiceStub asnc;
  private final CloudServiceBlockingStub blocking;
  private final Identity identity;

  public StubGrpcClient(ManagedChannel channel, CloudServiceStub async, CloudServiceBlockingStub blocking, Identity identity) {
    this.channel = channel;
    this.asnc = async;
    this.blocking = blocking;
    this.identity = identity;
  }

  @Override
  public void start() {
    channel.getState(true);
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

  public <Req, Rsp> Session onCall(Function<Req, CompletableFuture<Stream<Rsp>>> response) {
    AtomicReference<StreamObserver<RpcResponse>> replyStream = new AtomicReference<>();
    StreamObserver<RpcRequest> requestStream = new StreamObserver<>() {
      @Override
      public void onNext(RpcRequest value) {
        response.apply(map(value)).whenComplete((responsesStream, error) -> {
          if (error == null) {
            StreamObserver<RpcResponse> responseStream = replyStream.get();
            if (responseStream != null) {
              responsesStream.forEach(ans -> responseStream.onNext(map(ans)));
            }
          }
        });
      }

      @Override
      public void onError(Throwable t) {
      }

      @Override
      public void onCompleted() {
        // do we close reply stream ?
        replyStream.get().onCompleted();
      }

      private RpcResponse map(Rsp ans) {
        return null;
      }
      private Req map(RpcRequest value) {
        return null;
      }
    };

    StreamObserver<RpcResponse> observer = asnc.cloudToMinionRPC(requestStream);
    replyStream.set(observer);
    return new Session() {
      @Override
      public void close() throws IOException {
        requestStream.onCompleted();
        observer.onCompleted();
      }
    };
  }

  public <Msg> PublishSession<Msg> publishToCloud() {
    StreamObserver<MinionToCloudMessage> observer = asnc.minionToCloudMessages(new EmptyObserver());
    return new PublishSession<>() {
      @Override
      public void close() throws IOException {
        observer.onCompleted();
      }

      @Override
      public void publish(Msg message) {
        observer.onNext(map(message));
      }

      private <T> MinionToCloudMessage map(T message) {
        return null;
      }
    };
  }

  public <Msg> Session streamFromCloud(Consumer<Msg> consumer) {
    MinionHeader header = MinionHeader.newBuilder()
      .setLocation(identity.getLocation())
      .setSystemId(identity.getId())
      .build();

    StreamObserver<CloudToMinionMessage> observer = new ForwardingObserver<>(
      new Consumer<CloudToMinionMessage>() {
        @Override
        public void accept(CloudToMinionMessage cloudToMinionMessage) {
          consumer.accept(map(cloudToMinionMessage));
        }

        private Msg map(CloudToMinionMessage cloudToMinionMessage) {
          return null;
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
  public <Req, Rsp> Rsp request(Req request) {
    return mapResponse(blocking.minionToCloudRPC(mapRequest(request)));
  }

  private <Rsp> Rsp mapResponse(RpcResponse minionToCloudRPC) {
    return null;
  }

  private <Req> RpcRequest mapRequest(Req request) {
    return null;
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
