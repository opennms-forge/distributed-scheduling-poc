package org.opennms.poc.ignite.grpc.server.module;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import org.opennms.cloud.grpc.minion.RegistrationRequest;
import org.opennms.cloud.grpc.minion.RegistrationResponse;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.poc.ignite.grpc.server.GrpcServer;
import org.opennms.poc.ignite.grpc.server.ModuleHandler.IncomingRpcModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcRegistrationModule implements IncomingRpcModule<RegistrationRequest, RegistrationResponse> {

  public final static String MODULE_ID = "registration";

  private final Logger logger = LoggerFactory.getLogger(RpcRegistrationModule.class);
  private GrpcServer server;
  private int counter;

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public void start(GrpcServer server) {
    this.server = server;
  }

  @Override
  public CompletableFuture<RegistrationResponse> handle(RegistrationRequest request) {
    logger.info("Received registration request with payload {}", request);

    RegistrationResponse response = RegistrationResponse.newBuilder()
      .setInstance(request.getInstance())
      .setAck(counter++ % 2 == 0)
      .build();
    logger.info("Computed rpc response {}", response);
    return CompletableFuture.completedFuture(response);
  }

  @Override
  public Predicate<RpcRequest> predicate() {
    return request -> request.getModuleId().equals(MODULE_ID);
  }

  @Override
  public Class<RegistrationRequest> receive() {
    return RegistrationRequest.class;
  }

}
