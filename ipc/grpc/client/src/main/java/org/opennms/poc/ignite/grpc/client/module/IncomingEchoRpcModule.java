package org.opennms.poc.ignite.grpc.client.module;

import java.util.concurrent.CompletableFuture;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.MinionClient.IncomingRpcModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncomingEchoRpcModule implements IncomingRpcModule<RpcRequest, RpcResponse> {

  public final static String MODULE_ID = "echo";

  private final Logger logger = LoggerFactory.getLogger(IncomingEchoRpcModule.class);
  private GrpcClient client;

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public void start(GrpcClient client) {
    this.client = client;
    client.onCall(request -> MODULE_ID.equals(request.getModuleId()), this::handle);
  }

  @Override
  public CompletableFuture<RpcResponse> handle(RpcRequest request) {
    logger.info("Received echo request {}, publishing reply", request.getRpcId());
    return CompletableFuture.completedFuture(
      RpcResponse.newBuilder()
        .setRpcIdBytes(request.getRpcIdBytes())
        .setModuleIdBytes(request.getModuleIdBytes())
        .setLocationBytes(request.getLocationBytes())
        .setSystemIdBytes(request.getSystemIdBytes())
        .build()
    );
  }
}
