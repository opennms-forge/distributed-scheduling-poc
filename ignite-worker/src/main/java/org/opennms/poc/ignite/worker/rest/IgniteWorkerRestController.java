package org.opennms.poc.ignite.worker.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.opennms.poc.ignite.worker.workflows.Network;

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
    @Path("/hi-all-repeated-service")
    void deployHiAllRepeatedService();

    @DELETE
    @Path("/hi-all-repeated-service")
    void removeHiAllRepeatedService();

    @GET
    @Path("/noop-service/{count}")
    String deployNoopService(@PathParam("count") int count);

    @DELETE
    @Path("/noop-service/{count}")
    String undeployNoopService(@PathParam("count") int count);

    @GET
    @Path("/load-em-up")
    public void loadEmUp(Network.NetworkSize size);
}
