package org.opennms.poc.ignite.grpc.publisher;

import java.io.IOException;
import org.opennms.poc.ignite.model.workflows.Workflow;

public interface WorkflowPublisher {

  void publish(Workflow twin) throws IOException;

}
