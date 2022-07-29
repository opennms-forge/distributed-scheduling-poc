package org.opennms.core.ipc.grpc.server.manager.rpcstreaming.impl;

import static org.opennms.horizon.ipc.rpc.api.RpcModule.MINION_HEADERS_MODULE;

import com.google.common.base.Strings;
import io.grpc.stub.StreamObserver;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.core.ipc.grpc.server.manager.RpcConnectionTracker;
import org.opennms.core.ipc.grpc.server.manager.RpcRequestTracker;
import org.opennms.core.ipc.grpc.server.manager.rpcstreaming.MinionRpcStreamConnection;
import org.opennms.horizon.ipc.rpc.api.RpcResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC Streaming connection from a Minion.  Note that the Minion identity is expected to be the first message received,
 * so this connection needs to notify the Minion Manager of the minion's identity, and initiate wiring to the internal
 * connection tracker so requests to the minion can find this connection.
 */
public class MinionRpcStreamConnectionImpl implements MinionRpcStreamConnection {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(MinionRpcStreamConnectionImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private final StreamObserver<RpcRequestProto> streamObserver;
    private final Consumer<StreamObserver<RpcRequestProto>> onCompleted;
    private final BiConsumer<StreamObserver<RpcRequestProto>, Throwable> onError;
    private final RpcConnectionTracker rpcConnectionTracker;
    private final RpcRequestTracker rpcRequestTracker;
    private final ExecutorService responseHandlerExecutor;
    private final MinionManager minionManager;

    public MinionRpcStreamConnectionImpl(
            StreamObserver<RpcRequestProto> streamObserver,
            Consumer<StreamObserver<RpcRequestProto>> onCompleted,
            BiConsumer<StreamObserver<RpcRequestProto>, Throwable> onError,
            RpcConnectionTracker rpcConnectionTracker,
            RpcRequestTracker rpcRequestTracker,
            ExecutorService responseHandlerExecutor,
            MinionManager minionManager
            ) {

        this.streamObserver = streamObserver;
        this.onCompleted = onCompleted;
        this.onError = onError;
        this.rpcConnectionTracker = rpcConnectionTracker;
        this.rpcRequestTracker = rpcRequestTracker;
        this.responseHandlerExecutor = responseHandlerExecutor;
        this.minionManager = minionManager;
    }

    private boolean isMinionIndentityHeaders(RpcResponseProto rpcMessage) {
        return Objects.equals(MINION_HEADERS_MODULE, rpcMessage.getModuleId());
    }

    @Override
    public void handleRpcStreamInboundMessage(RpcResponseProto message) {
        if (isMinionIndentityHeaders(message)) {
            String location = message.getLocation();
            String systemId = message.getSystemId();

            if (Strings.isNullOrEmpty(location) || Strings.isNullOrEmpty(systemId)) {
                log.error("Invalid metadata received with location = {} , systemId = {}", location, systemId);
                return;
            }

            // Register the Minion
            boolean added = rpcConnectionTracker.addConnection(message.getLocation(), message.getSystemId(), streamObserver);

            if (added) {
                log.info("Added RPC handler for minion {} at location {}", systemId, location);

                // Notify the MinionManager of the addition
                MinionInfo minionInfo = new MinionInfo();
                minionInfo.setId(systemId);
                minionInfo.setLocation(location);
                minionManager.addMinion(minionInfo);
            }
        } else {
            // Schedule processing of the message which is expected to be a response to a past request sent to the
            //  Minion
            asyncQueueHandleResponse(message);
        }
    }

    @Override
    public void handleRpcStreamInboundError(Throwable thrown) {
        onError.accept(streamObserver, thrown);
    }

    @Override
    public void handleRpcStreamInboundCompleted() {
        onCompleted.accept(streamObserver);
    }

//========================================
// Internals
//----------------------------------------

    private void asyncQueueHandleResponse(RpcResponseProto message) {
        responseHandlerExecutor.execute(() -> syncHandleResponse(message));
    }

    private void syncHandleResponse(RpcResponseProto message) {
        if (Strings.isNullOrEmpty(message.getRpcId())) {
            return;
        }

        // Handle response from the Minion.
        RpcResponseHandler responseHandler = rpcRequestTracker.lookup(message.getRpcId());

        if (responseHandler != null && message.getRpcContent() != null) {
            responseHandler.sendResponse(message.getRpcContent().toStringUtf8());
        } else {
            log.debug("Received a response for request for module: {} with RpcId:{}, but no outstanding request was found with this id." +
                    "The request may have timed out", message.getModuleId(), message.getRpcId());
        }
    }
}
