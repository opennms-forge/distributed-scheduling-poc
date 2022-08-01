package org.opennms.core.ipc.grpc.server.manager.messaging;


import org.opennms.cloud.grpc.minion.RpcResponseProto;

public interface MinionMessagingConnection {
    void handleRpcStreamInboundMessage(RpcResponseProto message);

    void handleRpcStreamInboundError(Throwable thrown);

    void handleRpcStreamInboundCompleted();
}
