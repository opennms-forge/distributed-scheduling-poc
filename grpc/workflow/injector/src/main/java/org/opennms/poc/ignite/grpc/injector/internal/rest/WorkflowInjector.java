package org.opennms.poc.ignite.grpc.injector.internal.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.opennms.poc.ignite.grpc.model.WorkflowTwin;

@Path("/injector/workflow")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface WorkflowInjector {

  @POST
  void inject(WorkflowTwin model);

}
