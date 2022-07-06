package org.opennms.poc.ignite.grpc.publisher;

import java.io.IOException;
import org.opennms.poc.ignite.grpc.model.WorkflowTwin;

public interface WorkflowPublisher {

  void publish(WorkflowTwin twin) throws IOException;

}
