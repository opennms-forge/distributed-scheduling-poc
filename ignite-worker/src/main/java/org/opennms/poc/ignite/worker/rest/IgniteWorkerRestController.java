package org.opennms.poc.ignite.worker.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/poc")
public interface IgniteWorkerRestController {

    @GET
    @Path("/hi-youngest")
    void hiOnYoungest();

    @GET
    @Path("/hi-oldest")
    void hiOnOldest();

    @GET
    @Path("/hi-all")
    void hiAll();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/service-deployment/metrics")
    Response reportServiceDeploymentMetrics(@QueryParam("verbose") boolean verbose);
}
