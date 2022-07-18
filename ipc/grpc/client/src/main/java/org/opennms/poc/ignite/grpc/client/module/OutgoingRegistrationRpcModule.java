package org.opennms.poc.ignite.grpc.client.module;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.UUID;
import org.opennms.cloud.grpc.minion.RegistrationRequest;
import org.opennms.cloud.grpc.minion.RegistrationResponse;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.MinionClient.OutgoingRpcModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutgoingRegistrationRpcModule implements OutgoingRpcModule<RpcRequest, RpcResponse> {

  public final static String MODULE_ID = "registration";

  private final Logger logger = LoggerFactory.getLogger(OutgoingRegistrationRpcModule.class);

  private GrpcClient client;

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public void start(GrpcClient client) {
    this.client = client;

    RegistrationRequest request = RegistrationRequest.newBuilder()
      .setInstance("my-test-instance")
      .build();

    RpcRequest rpcRequest = RpcRequest.newBuilder()
      .setModuleId(MODULE_ID)
      .setRpcId(UUID.randomUUID().toString())
      .setRpcContent(Any.pack(request))
      .build();

    RpcResponse response = client.request(rpcRequest);
    if (response.getRpcContent().is(RegistrationResponse.class)) {
      try {
        RegistrationResponse status = response.getRpcContent().unpack(RegistrationResponse.class);
        logger.debug("Registration confirmed: {}", status.getAck());
      } catch (InvalidProtocolBufferException e) {
        logger.error("Failed to complete registration", e);
      }
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
