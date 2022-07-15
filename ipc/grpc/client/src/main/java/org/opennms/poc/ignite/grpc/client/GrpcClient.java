package org.opennms.poc.ignite.grpc.client;

import io.grpc.ConnectivityState;
import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface GrpcClient extends AutoCloseable {

  // CloudToMinionRPC, async
  <Req, Rsp> Session onCall(Function<Req, CompletableFuture<Stream<Rsp>>> response);
  // CloudToMinionMessages, async
  <Msg> Session streamFromCloud(Consumer<Msg> consumer);

  // MinionToCloudRPC, blocking
  <Req, Rsp>  Rsp request(Req request);
  // MinionToCloudMessages
  <Msg> PublishSession<Msg> publishToCloud();

  void start();

  ConnectivityState getState();

  interface Session extends Closeable { }

  interface PublishSession<T> extends Session {
    void publish(T message);
  }

}
