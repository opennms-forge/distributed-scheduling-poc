package org.opennms.core.ipc.grpc.server.manager.rpcstreaming.impl;

import io.grpc.stub.StreamObserver;
import java.util.concurrent.ExecutorService;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.core.ipc.grpc.server.manager.RpcConnectionTracker;
import org.opennms.core.ipc.grpc.server.manager.RpcRequestTracker;
import org.opennms.core.ipc.grpc.server.manager.adapter.InboundRpcAdapter;
import org.opennms.core.ipc.grpc.server.manager.rpcstreaming.MinionRpcStreamConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinionRpcStreamConnectionManagerImpl implements MinionRpcStreamConnectionManager {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(MinionRpcStreamConnectionManagerImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private final RpcConnectionTracker rpcConnectionTracker;
    private final RpcRequestTracker rpcRequestTracker;
    private final MinionManager minionManager;
    private final ExecutorService responseHandlerExecutor;


//========================================
// Constructor
//----------------------------------------

    public MinionRpcStreamConnectionManagerImpl(
            RpcConnectionTracker rpcConnectionTracker,
            RpcRequestTracker rpcRequestTracker,
            MinionManager minionManager,
            ExecutorService responseHandlerExecutor) {

        this.rpcConnectionTracker = rpcConnectionTracker;
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
    public InboundRpcAdapter startRpcStreaming(StreamObserver<RpcRequestProto> requestObserver) {
        MinionRpcStreamConnectionImpl connection =
                new MinionRpcStreamConnectionImpl(
                        requestObserver,
                        this::onConnectionCompleted,
                        this::onError,
                        rpcConnectionTracker,
                        rpcRequestTracker,
                        responseHandlerExecutor,
                        minionManager
                        );

        InboundRpcAdapter result =
                new InboundRpcAdapter(
                        connection::handleRpcStreamInboundMessage,
                        connection::handleRpcStreamInboundError,
                        connection::handleRpcStreamInboundCompleted
                );

        return result;
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

    private void onConnectionCompleted(StreamObserver<RpcRequestProto> streamObserver) {
        log.info("Minion RPC handler closed");
        MinionInfo removedMinionInfo = rpcConnectionTracker.removeConnection(streamObserver);

        // Notify the MinionManager of the removal
        if (removedMinionInfo.getId() != null) {
            minionManager.removeMinion(removedMinionInfo.getId());
        }
    }
}
