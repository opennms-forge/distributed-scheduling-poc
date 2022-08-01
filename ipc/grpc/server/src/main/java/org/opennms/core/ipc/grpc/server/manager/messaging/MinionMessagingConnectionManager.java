package org.opennms.core.ipc.grpc.server.manager.messaging;

import io.grpc.stub.StreamObserver;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.core.ipc.grpc.server.manager.adapter.InboundRpcAdapter;

public interface MinionMessagingConnectionManager {
    InboundRpcAdapter startMinionStreaming(StreamObserver<MinionToCloudMessage> messageStreamObserver);
}
