package org.opennms.poc.ignite.grpc.client.module;

import java.util.concurrent.CompletableFuture;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.MinionClient.IncomingRpcModule;

public class IncomingEchoRpcModule implements IncomingRpcModule<RpcRequest, RpcResponse> {

  public final static String MODULE_ID = "echo";

  private GrpcClient client;

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public void start(GrpcClient client) {
    this.client = client;
  }

  @Override
  public CompletableFuture<RpcResponse> handle(RpcRequest request) {
    return CompletableFuture.completedFuture(
      RpcResponse.newBuilder()
        .setRpcIdBytes(request.getRpcIdBytes())
        .setLocationBytes(request.getLocationBytes())
        .setSystemIdBytes(request.getSystemIdBytes())
        .build()
    );
  }
}
