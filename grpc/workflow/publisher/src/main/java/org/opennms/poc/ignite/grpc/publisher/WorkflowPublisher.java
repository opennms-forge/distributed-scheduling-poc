package org.opennms.poc.ignite.grpc.publisher;

import java.io.IOException;
import org.opennms.poc.ignite.model.workflows.Workflows;

public interface WorkflowPublisher {

  void publish(Workflows twin) throws IOException;

}
