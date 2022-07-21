package org.opennms.core.ipc.grpc.server.manager.adapter;

import io.grpc.stub.StreamObserver;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceImplBase;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.Empty;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;

public class MinionRSTransportAdapter extends CloudServiceImplBase {

    private final Function<StreamObserver<RpcRequestProto>, StreamObserver<RpcResponseProto>> cloudToMinionRPC;
    private final BiConsumer<MinionHeader, StreamObserver<CloudToMinionMessage>> cloudToMinionMessages;
    private final BiConsumer<RpcRequestProto, StreamObserver<RpcResponseProto>> minionToCloudRPC;
    private final Function<StreamObserver<Empty>, StreamObserver<MinionToCloudMessage>> minionToCloudMessages;

    public MinionRSTransportAdapter(Function<StreamObserver<RpcRequestProto>, StreamObserver<RpcResponseProto>> cloudToMinionRPC,
        BiConsumer<MinionHeader, StreamObserver<CloudToMinionMessage>> cloudToMinionMessages,
        BiConsumer<RpcRequestProto, StreamObserver<RpcResponseProto>> minionToCloudRPC,
        Function<StreamObserver<Empty>, StreamObserver<MinionToCloudMessage>> minionToCloudMessages) {
        this.cloudToMinionRPC = cloudToMinionRPC;
        this.cloudToMinionMessages = cloudToMinionMessages;
        this.minionToCloudRPC = minionToCloudRPC;
        this.minionToCloudMessages = minionToCloudMessages;
    }

    @Override
    public StreamObserver<RpcResponseProto> cloudToMinionRPC(StreamObserver<RpcRequestProto> responseObserver) {
        return cloudToMinionRPC.apply(responseObserver);
    }

    @Override
    public void cloudToMinionMessages(MinionHeader request, StreamObserver<CloudToMinionMessage> responseObserver) {
        cloudToMinionMessages.accept(request, responseObserver);
    }

    @Override
    public void minionToCloudRPC(RpcRequestProto request, StreamObserver<RpcResponseProto> responseObserver) {
        minionToCloudRPC.accept(request, responseObserver);
    }

    @Override
    public StreamObserver<MinionToCloudMessage> minionToCloudMessages(StreamObserver<Empty> responseObserver) {
        return minionToCloudMessages.apply(responseObserver);
    }

}
