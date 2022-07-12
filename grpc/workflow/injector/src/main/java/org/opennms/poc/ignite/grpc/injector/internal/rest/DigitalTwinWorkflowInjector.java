package org.opennms.poc.ignite.grpc.injector.internal.rest;

import java.io.IOException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.opennms.poc.ignite.grpc.publisher.WorkflowPublisher;
import org.opennms.poc.ignite.model.workflows.Workflows;

public class DigitalTwinWorkflowInjector implements WorkflowInjector {

  private final WorkflowPublisher publisher;

  public DigitalTwinWorkflowInjector(WorkflowPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public void inject(Workflows model) {
    try {
      publisher.publish(model);
    } catch (IOException e) {
      throw new WebApplicationException("Failed to inject data", Response.serverError().entity(e).build());
    }
  }

}
