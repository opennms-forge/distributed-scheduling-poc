package org.opennms.poc.ignite.grpc.client;

import com.google.protobuf.Any;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceStub;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.cloud.grpc.minion.SinkMessage;
import org.opennms.poc.ignite.grpc.client.internal.DelegatingClientInterceptor.GrpcClientRequestInterceptor;
import org.opennms.poc.ignite.grpc.client.internal.DelegatingClientInterceptor.GrpcClientResponseInterceptor;

public class Client {

  public static void main(String[] args) throws Exception {
    ManagedChannel channel = ManagedChannelBuilder.forTarget("127.0.0.1:8080").usePlaintext()
      .intercept(new GrpcClientRequestInterceptor(), new GrpcClientResponseInterceptor())
      .build();
    System.out.println(channel.getState(true));

    CloudServiceStub asyncStub = CloudServiceGrpc.newStub(channel);

    // cloud to minion
    System.out.println("cloud to minion");
    asyncStub.cloudToMinionRPC(new DebugObserver<>())
      .onNext(RpcResponseProto.newBuilder().setLocation("test").build());

    // minion to cloud
    System.out.println("minion to cloud");
    RpcRequestProto request = RpcRequestProto.newBuilder().setLocation("test").build();
    asyncStub.minionToCloudRPC(request, new DebugObserver<>());

    // cloud to minion
    System.out.println("cloud to minion message");
    MinionHeader header = MinionHeader.newBuilder()
      .setSystemId("Test")
      .setLocation("DC1")
      .build();
    asyncStub.cloudToMinionMessages(header, new DebugObserver<>());

    MinionToCloudMessage message = MinionToCloudMessage.newBuilder()
      .setSinkMessage(SinkMessage.newBuilder().setMessageId("test").build())
      .build();

    // minion to cloud
    System.out.println("minion to cloud message");
    StreamObserver<MinionToCloudMessage> observer = asyncStub.minionToCloudMessages(new DebugObserver<>());
    observer.onNext(message);


    Thread.sleep(10000);
    channel.shutdown();
  }

  static class DebugObserver<T> implements StreamObserver<T> {
    @Override
    public void onNext(T t) {
      System.out.println("Received " + t);
    }

    @Override
    public void onError(Throwable throwable) {
      System.err.println("Error " + throwable);
    }

    @Override
    public void onCompleted() {
      System.out.println("Completed");
    }
  }

}
