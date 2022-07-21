package org.opennms.poc.ignite.grpc.server;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerCredentials;
import io.grpc.stub.StreamObserver;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceImplBase;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.Empty;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.cloud.grpc.minion.TwinRequestProto;
import org.opennms.cloud.grpc.minion.TwinResponseProto;

public class Main {

  public static void main(String[] args) throws Exception {
    ServerCredentials credentials = InsecureServerCredentials.create();

    Server server = Grpc.newServerBuilderForPort(8080, credentials)
      .addService(new StreamHandler())
      .build();

    server.start();

    System.in.read();
  }

  static class StreamHandler extends CloudServiceImplBase {

    @Override
    public StreamObserver<RpcResponseProto> cloudToMinionRPC(StreamObserver<RpcRequestProto> responseObserver) {
      System.out.println("cloudToMinionRPC received " + responseObserver);
      //return super.cloudToMinionRPC(responseObserver);
      RpcResponseProto response = RpcResponseProto.newBuilder()
        .setLocation("server answer")
        .build();
      DummyStreamObserver<RpcResponseProto> observer = new DummyStreamObserver<>();

      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(5000);
            observer.onNext(response);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }, "publisher").start();

      return observer;
    }

    @Override
    public void minionToCloudRPC(RpcRequestProto request, StreamObserver<RpcResponseProto> responseObserver) {
      System.out.println("minionToCloudRPC received " + request + " " + responseObserver);
      //super.minionToCloudRPC(request, responseObserver);

      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(5000);
            RpcResponseProto rpcResponse = RpcResponseProto.newBuilder().setLocation("test").build();
            responseObserver.onNext(rpcResponse);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }, "publisher").start();
    }

    @Override
    public void cloudToMinionMessages(MinionHeader request, StreamObserver<CloudToMinionMessage> responseObserver) {
      System.out.println("cloudToMinionMessages received " + request + " " + responseObserver);
      //super.cloudToMinionMessages(request, responseObserver);
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(5000);
            CloudToMinionMessage rpcResponse = CloudToMinionMessage.newBuilder().setTwinResponse(
              TwinResponseProto.newBuilder().setConsumerKey("workflow").build()
            ).build();
            responseObserver.onNext(rpcResponse);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }, "publisher").start();
    }

    @Override
    public StreamObserver<MinionToCloudMessage> minionToCloudMessages(StreamObserver<Empty> responseObserver) {
      System.out.println("minionToCloudMessages received " + responseObserver);
      //return super.minionToCloudMessages(responseObserver);
      return new DummyStreamObserver<>();
    }
  }

  static class DummyStreamObserver<T> implements StreamObserver<T> {

    @Override
    public void onNext(T t) {
      System.out.println("Dummy payload " + t);
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
