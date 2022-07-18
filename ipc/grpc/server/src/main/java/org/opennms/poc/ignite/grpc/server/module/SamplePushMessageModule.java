package org.opennms.poc.ignite.grpc.server.module;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.TwinRequestProto;
import org.opennms.poc.ignite.grpc.server.GrpcServer;
import org.opennms.poc.ignite.grpc.server.ModuleHandler.PushModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamplePushMessageModule implements PushModule<CloudToMinionMessage>, Runnable {

  public final static String MODULE_ID = "push_to_minion";

  private final Logger logger = LoggerFactory.getLogger(SamplePushMessageModule.class);
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "server-push"));
  private GrpcServer server;

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public void start(GrpcServer server) {
    this.server = server;

    executor.scheduleAtFixedRate(this, 5, 30, TimeUnit.SECONDS);
  }

  @Override
  public void run() {
    TwinRequestProto twinRequestProto = TwinRequestProto.newBuilder()
      .setConsumerKey("test")
      .build();

    CloudToMinionMessage cloudToMinionMessage = CloudToMinionMessage.newBuilder()
      .setTwinRequest(twinRequestProto)
      .build();
    // broadcast to all nodes
    server.broadcast(cloudToMinionMessage).whenComplete((time, error) -> {
      if (error != null) {
        logger.error("Failed to publish message", error);
        return;
      }
      logger.info("Published message {} to all nodes", twinRequestProto);
    });
  }

}
