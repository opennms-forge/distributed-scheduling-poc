package org.opennms.poc.ignite.grpc.client;

import io.grpc.ConnectivityState;
import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;

public interface GrpcClient extends AutoCloseable {

  // CloudToMinionRPC, async
  Session onCall(Predicate<RpcRequest> request, Function<RpcRequest, CompletableFuture<RpcResponse>> response);
  // CloudToMinionMessages, async
  Session streamFromCloud(Consumer<CloudToMinionMessage> consumer);

  // MinionToCloudRPC, blocking
  RpcResponse request(RpcRequest request);
  // MinionToCloudMessages
  PublishSession publishToCloud();

  void start();

  ConnectivityState getState();

  interface Session extends Closeable { }

  interface PublishSession extends Session {
    void publish(MinionToCloudMessage message);
  }

}
