package org.opennms.poc.ignite.grpc.server.module;

import java.util.function.Predicate;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.poc.ignite.grpc.server.GrpcServer;
import org.opennms.poc.ignite.grpc.server.ModuleHandler.CollectorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SinkMessageCollectorModule implements CollectorModule<MinionToCloudMessage> {

  public final static String MODULE_ID = "push_from_minion";

  private final Logger logger = LoggerFactory.getLogger(SinkMessageCollectorModule.class);
  private GrpcServer server;

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public void start(GrpcServer server) {
    this.server = server;
  }

  @Override
  public Predicate<MinionToCloudMessage> predicate() {
    return message -> message.hasSinkMessage() && message.getSinkMessage().getModuleId().equals(MODULE_ID);
  }

  @Override
  public void handle(MinionToCloudMessage message) {
    logger.info("Received sink message from minion {}", message);
  }

}
