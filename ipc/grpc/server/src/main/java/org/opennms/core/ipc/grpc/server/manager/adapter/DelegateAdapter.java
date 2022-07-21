package org.opennms.core.ipc.grpc.server.manager.adapter;

import io.grpc.stub.StreamObserver;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceImplBase;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.Empty;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;

public class DelegateAdapter extends CloudServiceImplBase {

    private final CloudServiceDelegate delegate;

    public DelegateAdapter(CloudServiceDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public StreamObserver<RpcResponseProto> cloudToMinionRPC(StreamObserver<RpcRequestProto> responseObserver) {
        return delegate.cloudToMinionRPC(responseObserver);
    }

    @Override
    public void cloudToMinionMessages(MinionHeader request, StreamObserver<CloudToMinionMessage> responseObserver) {
        delegate.cloudToMinionMessages(request, responseObserver);
    }

    @Override
    public void minionToCloudRPC(RpcRequestProto request, StreamObserver<RpcResponseProto> responseObserver) {
        delegate.minionToCloudRPC(request, responseObserver);
    }

    @Override
    public StreamObserver<MinionToCloudMessage> minionToCloudMessages(StreamObserver<Empty> responseObserver) {
        return delegate.minionToCloudMessages(responseObserver);
    }

}
