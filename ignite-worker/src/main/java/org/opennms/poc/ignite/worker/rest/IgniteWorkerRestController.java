package org.opennms.poc.ignite.worker.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
}
