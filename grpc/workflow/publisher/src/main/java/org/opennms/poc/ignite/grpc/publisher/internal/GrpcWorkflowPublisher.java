package org.opennms.poc.ignite.grpc.publisher.internal;

import java.io.IOException;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinPublisher.Session;
import org.opennms.poc.ignite.grpc.model.WorkflowTwin;
import org.opennms.poc.ignite.grpc.publisher.WorkflowPublisher;

public class GrpcWorkflowPublisher implements WorkflowPublisher {

  private final TwinPublisher publisher;
  private Session<WorkflowTwin> session;

  public GrpcWorkflowPublisher(TwinPublisher publisher) {
    this.publisher = publisher;
  }

  public void start() throws Exception {
    session = publisher.register("workflow", WorkflowTwin.class);
  }

  @Override
  public void publish(WorkflowTwin twin) throws IOException {
    session.publish(twin);
  }

}
