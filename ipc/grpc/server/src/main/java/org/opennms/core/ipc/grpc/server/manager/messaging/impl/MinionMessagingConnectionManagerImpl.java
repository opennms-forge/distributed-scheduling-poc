package org.opennms.core.ipc.grpc.server.manager.messaging.impl;

import io.grpc.stub.StreamObserver;
import java.util.concurrent.ExecutorService;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.core.ipc.grpc.server.manager.MessagingConnectionTracker;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.core.ipc.grpc.server.manager.RpcConnectionTracker;
import org.opennms.core.ipc.grpc.server.manager.RpcRequestTracker;
import org.opennms.core.ipc.grpc.server.manager.adapter.InboundRpcAdapter;
import org.opennms.core.ipc.grpc.server.manager.messaging.MinionMessagingConnectionManager;
import org.opennms.horizon.ipc.sink.api.MessageConsumerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinionMessagingConnectionManagerImpl implements MinionMessagingConnectionManager {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(
        MinionMessagingConnectionManagerImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private final RpcConnectionTracker rpcConnectionTracker;
    private final MessagingConnectionTracker messagingConnectionTracker;
    private final RpcRequestTracker rpcRequestTracker;
    private final MinionManager minionManager;
    private final ExecutorService responseHandlerExecutor;


//========================================
// Constructor
//----------------------------------------

    public MinionMessagingConnectionManagerImpl(
            RpcConnectionTracker rpcConnectionTracker,
            MessagingConnectionTracker messagingConnectionTracker,
            RpcRequestTracker rpcRequestTracker,
            MinionManager minionManager,
            ExecutorService responseHandlerExecutor) {

        this.rpcConnectionTracker = rpcConnectionTracker;
        this.messagingConnectionTracker = messagingConnectionTracker;
        this.rpcRequestTracker = rpcRequestTracker;
        this.minionManager = minionManager;
        this.responseHandlerExecutor = responseHandlerExecutor;
    }


//========================================
// Lifecycle
//----------------------------------------

    public void shutdown() {
        responseHandlerExecutor.shutdown();
    }


//========================================
// Processing
//----------------------------------------

    @Override
    public InboundRpcAdapter startMinionStreaming(StreamObserver<MinionToCloudMessage> messageStreamObserver) {
//        MinionMessagingConnectionManager connection =
//            new MinionMessagingConnectionManagerImpl(
//                messageStreamObserver,
//                null, //this::onMessagingConnectionCompleted,
//                null, //this::onError,
//                rpcConnectionTracker,
//                rpcRequestTracker,
//                responseHandlerExecutor,
//                minionManager
//            );

//        InboundRpcAdapter result =new InboundRpcAdapter(
//            connection::handleRpcStreamInboundMessage,
//            connection::handleRpcStreamInboundError,
//            connection::handleRpcStreamInboundCompleted
//        );

//        return result;
        return null;
    }

  private void onError(StreamObserver<RpcRequestProto> streamObserver, Throwable throwable) {
    log.info("Minion RPC handler reported an error");
    MinionInfo removedMinionInfo = rpcConnectionTracker.removeConnection(streamObserver);

    // Notify the MinionManager of the removal
    if (removedMinionInfo.getId() != null) {
      minionManager.removeMinion(removedMinionInfo.getId());
    }
  }

//========================================
// Internals
//----------------------------------------

    private void onRpcConnectionCompleted(StreamObserver<RpcRequestProto> streamObserver) {
        log.info("Minion RPC handler closed");
        MinionInfo removedMinionInfo = rpcConnectionTracker.removeConnection(streamObserver);

        // Notify the MinionManager of the removal
        if (removedMinionInfo.getId() != null) {
            minionManager.removeMinion(removedMinionInfo.getId());
        }
    }

    private void onMessagingConnectionCompleted(StreamObserver<MinionToCloudMessage> streamObserver) {
        log.info("Minion RPC handler closed");
//        MinionInfo removedMinionInfo = rpcConnectionTracker.removeConnection(streamObserver);

        // Notify the MinionManager of the removal
//        if (removedMinionInfo.getId() != null) {
//            minionManager.removeMinion(removedMinionInfo.getId());
//        }
    }
}
