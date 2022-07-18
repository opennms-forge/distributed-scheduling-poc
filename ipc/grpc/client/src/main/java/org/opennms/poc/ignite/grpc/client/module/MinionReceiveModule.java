package org.opennms.poc.ignite.grpc.client.module;

import java.util.function.Consumer;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.GrpcClient.PublishSession;
import org.opennms.poc.ignite.grpc.client.MinionClient.SinkModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinionReceiveModule implements SinkModule, Consumer<CloudToMinionMessage> {

  public final static String MODULE_ID = "push_to_minion";

  private final Logger logger = LoggerFactory.getLogger(MinionReceiveModule.class);
  private PublishSession session;
  private GrpcClient client;

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public void start(GrpcClient client) {
    this.client = client;
    client.streamFromCloud(this);
  }

  @Override
  public void accept(CloudToMinionMessage message) {
    logger.info("Received cloud message {}", message);
  }
}
