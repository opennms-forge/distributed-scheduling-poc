package org.opennms.poc.ignite.grpc.client.module;

import com.google.protobuf.ByteString;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.SinkMessage;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.GrpcClient.PublishSession;
import org.opennms.poc.ignite.grpc.client.MinionClient.PublishModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinionPushModule implements PublishModule, Runnable {

  public final static String MODULE_ID = "push_from_minion";

  private final Logger logger = LoggerFactory.getLogger(MinionPushModule.class);
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "minion-publish"));

  private PublishSession session;

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public void start(GrpcClient client) {
    session = client.publishToCloud();

    executor.scheduleAtFixedRate(this, 5, 30, TimeUnit.SECONDS);
  }

  @Override
  public void run() {
    if (session == null) {
      return;
    }

    SinkMessage sinkMessage = SinkMessage.newBuilder()
      .setModuleId(MODULE_ID)
      .setMessageId(UUID.randomUUID().toString())
      .setContent(ByteString.copyFromUtf8("{\"test\": [0, 1, 2, 3]}"))
      .build();

    MinionToCloudMessage cloudMessage = MinionToCloudMessage.newBuilder()
      .setSinkMessage(sinkMessage)
      .build();

    session.publish(cloudMessage);
    logger.info("Published sink message to cloud {}", sinkMessage);
  }

}
