package org.opennms.poc.ignite.grpc.client.module;

import com.google.protobuf.ByteString;
import java.util.UUID;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.MinionClient.OutgoingRpcModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutgoingRegistrationRpcModule implements OutgoingRpcModule<RpcRequest, RpcResponse> {

  public final static String MODULE_ID = "echo";

  private final Logger logger = LoggerFactory.getLogger(OutgoingRegistrationRpcModule.class);

  private GrpcClient client;

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public void start(GrpcClient client) {
    this.client = client;
    RpcResponse response = client.request(RpcRequest.newBuilder()
      .setModuleId(MODULE_ID)
      .setRpcId(UUID.randomUUID().toString())
      .setRpcContent(ByteString.copyFromUtf8("hello"))
    );
    if (response.getRpcContent().toString().equals("hello")) {
      logger.debug("Registration successful");
    }
  }

  @Override
  public Class<RpcRequest> payload() {
    return RpcRequest.class;
  }

  @Override
  public Class<RpcResponse> ack() {
    return RpcResponse.class;
  }
}
