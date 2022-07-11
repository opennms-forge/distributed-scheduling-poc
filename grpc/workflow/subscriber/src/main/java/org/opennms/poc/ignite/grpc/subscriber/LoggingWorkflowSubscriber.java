package org.opennms.poc.ignite.grpc.subscriber;

import org.opennms.poc.ignite.grpc.whiteboard.api.MessageListener;
import org.opennms.poc.ignite.model.workflows.Workflows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingWorkflowSubscriber implements MessageListener<Workflows> {

  private final Logger logger = LoggerFactory.getLogger(LoggingWorkflowSubscriber.class);

  @Override
  public void accept(Workflows workflowTwin) {
    logger.info(">> Received workflow twin {}", workflowTwin);
  }

  @Override
  public Class<Workflows> getType() {
    return Workflows.class;
  }
}
