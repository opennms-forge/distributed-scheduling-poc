package org.opennms.poc.ignite.grpc.subscriber;

import java.util.function.Consumer;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.poc.ignite.grpc.model.WorkflowTwin;

public class WorkflowSubscriber implements Consumer<WorkflowTwin> {

  private final TwinSubscriber twinSubscriber;

  public WorkflowSubscriber(TwinSubscriber twinSubscriber) {
    this.twinSubscriber = twinSubscriber;
  }

  public void start() {
    twinSubscriber.subscribe("workflow", WorkflowTwin.class, this);
  }

  @Override
  public void accept(WorkflowTwin workflowTwin) {
    System.out.println("Received workflow twin " + workflowTwin);
  }

}
