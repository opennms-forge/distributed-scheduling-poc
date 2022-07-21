package org.opennms.core.ipc.grpc.server.manager.adapter;

import io.grpc.stub.StreamObserver;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.Empty;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;

public interface CloudServiceDelegate {

  StreamObserver<RpcResponseProto> cloudToMinionRPC(StreamObserver<RpcRequestProto> responseObserver);

  void cloudToMinionMessages(MinionHeader request, StreamObserver<CloudToMinionMessage> responseObserver);

  void minionToCloudRPC(RpcRequestProto request, StreamObserver<RpcResponseProto> responseObserver);

  StreamObserver<MinionToCloudMessage> minionToCloudMessages(StreamObserver<Empty> responseObserver);
}
