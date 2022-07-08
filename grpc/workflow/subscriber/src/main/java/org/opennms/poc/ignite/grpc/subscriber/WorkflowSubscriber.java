package org.opennms.poc.ignite.grpc.subscriber;

import java.util.function.Consumer;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowSubscriber implements Consumer<Workflow> {

  private final Logger logger = LoggerFactory.getLogger(WorkflowSubscriber.class);

  private final TwinSubscriber twinSubscriber;

  public WorkflowSubscriber(TwinSubscriber twinSubscriber) {
    this.twinSubscriber = twinSubscriber;
  }

  public void start() {
    twinSubscriber.subscribe("workflow", Workflow.class, this);
  }

  @Override
  public void accept(Workflow workflowTwin) {
    logger.info(">> Received workflow twin {}", workflowTwin);
  }

}
