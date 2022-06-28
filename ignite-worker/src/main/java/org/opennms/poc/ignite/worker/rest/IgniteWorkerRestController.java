package org.opennms.poc.ignite.worker.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.opennms.poc.ignite.worker.workflows.Network;

@Path("/poc")
public interface IgniteWorkerRestController {

    //    @GetMapping(path = "/hi-youngest")
    void hiOnYoungest();
    @GET
    @Path("/hi-oldest")

    //    @GetMapping(path = "/hi-oldest")
    void hiOnOldest();

    //    @GetMapping(path = "/hi-all")
    @GET
    @Path("/hi-all")
    void hiAll();

    //    @GetMapping(path = "/hi-all-repeated-service")
    @GET
    @Path("/hi-all-repeated-service")
    void deployHiAllRepeatedService();

    //    @DeleteMapping(path = "/hi-all-repeated-service")
    @DELETE
    @Path("/hi-all-repeated-service")
    void removeHiAllRepeatedService();

    //    @GetMapping(path = "/noop-service")
    @GET
    @Path("/noop-service/{count}")
    String deployNoopService(/*@RequestParam(value = "count", defaultValue = "1")*/ @PathParam("count") int count);

    //    @DeleteMapping(path = "/noop-service")
    @DELETE
    @Path("/noop-service/{count}")
    String undeployNoopService(/*@RequestParam(value = "count", defaultValue = "1")*/ @PathParam("count") int count);

    //    @GetMapping(path = "/load-em-up")
    @GET
    @Path("/load-em-up/{size}")
    void loadEmUp(/*@RequestParam(value = "size", defaultValue = "SMALL")*/ @PathParam("size") Network.NetworkSize size);
}
