package org.opennms.poc.ignite.grpc.publisher.internal;

import java.io.IOException;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinPublisher.Session;
import org.opennms.poc.ignite.grpc.publisher.WorkflowPublisher;
import org.opennms.poc.ignite.model.workflows.Workflow;

public class GrpcWorkflowPublisher implements WorkflowPublisher {

  private final TwinPublisher publisher;
  private Session<Workflow> session;

  public GrpcWorkflowPublisher(TwinPublisher publisher) {
    this.publisher = publisher;
  }

  public void start() throws Exception {
    session = publisher.register("workflow", Workflow.class);
  }

  @Override
  public void publish(Workflow twin) throws IOException {
    session.publish(twin);
  }

}
