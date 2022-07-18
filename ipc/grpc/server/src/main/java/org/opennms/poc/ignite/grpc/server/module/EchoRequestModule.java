package org.opennms.poc.ignite.grpc.server.module;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.poc.ignite.grpc.server.GrpcServer;
import org.opennms.poc.ignite.grpc.server.ModuleHandler.OutgoingRpcModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoRequestModule implements OutgoingRpcModule, Runnable {

  public final static String MODULE_ID = "echo";

  private final Logger logger = LoggerFactory.getLogger(EchoRequestModule.class);
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "echo-requester"));

  private GrpcServer server;

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public void start(GrpcServer server) {
    this.server = server;
    executor.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
  }

  @Override
  public void run() {
    RpcRequest request = RpcRequest.newBuilder()
      .setModuleId(MODULE_ID)
      .setSystemId("minion01")
      .setLocation("dc1")
      .setRpcId(UUID.randomUUID().toString())
      .build();

    logger.info("Requesting echo from test minion {}", request);
    CompletableFuture<RpcResponse> response = server.request("minion01", "dc1", request);
    response.whenComplete((reply, error) -> {
      if (error != null) {
        if (error instanceof TimeoutException) {
          logger.info("Failed to retrieve test minion answer");
          return;
        }
        logger.error("Failed to retrieve minion answer", error);
        return;
      }

      logger.info("Received answer {} for rpc request {}", reply, request);
    });
  }
}
