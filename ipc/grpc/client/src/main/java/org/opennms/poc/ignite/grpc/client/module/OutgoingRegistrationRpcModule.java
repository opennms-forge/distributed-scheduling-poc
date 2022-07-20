package org.opennms.poc.ignite.grpc.client.module;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.UUID;
import org.opennms.cloud.grpc.minion.RegistrationRequest;
import org.opennms.cloud.grpc.minion.RegistrationResponse;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.MinionClient.OutgoingRpcModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutgoingRegistrationRpcModule implements OutgoingRpcModule<RpcRequestProto, RpcResponseProto> {

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

    RpcRequestProto rpcRequest = RpcRequestProto.newBuilder()
      .setModuleId(MODULE_ID)
      .setRpcId(UUID.randomUUID().toString())
      .setRpcContent(Any.pack(request).getValue())
      .build();

    RpcResponseProto response = client.request(rpcRequest);
    if (response.getRpcContent() != null) {
      try {
        RegistrationResponse status = RegistrationResponse.newBuilder().mergeFrom(response.getRpcContent()).build();
        logger.debug("Registration confirmed: {}", status.getAck());
      } catch (InvalidProtocolBufferException e) {
        logger.error("Failed to complete registration", e);
      }
    }
  }

  @Override
  public Class<RpcRequestProto> payload() {
    return RpcRequestProto.class;
  }

  @Override
  public Class<RpcResponseProto> ack() {
    return RpcResponseProto.class;
  }
}
