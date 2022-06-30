package org.opennms.poc.ignite.worker.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.opennms.poc.ignite.worker.workflows.Network;

@Path("/poc")
public interface IgniteWorkerRestController {

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

            ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
            serviceConfiguration.setName(serviceName);
            serviceConfiguration.setService(new NoopService(lastServiceName));

            serviceConfiguration.setTotalCount(1);
            ignite.services().deployAsync(serviceConfiguration);

            cur++;
        }

        String msg = "STARTED " + cur + " NO-OP SERVICE INSTANCES";
        System.out.println(msg);

        return msg;
    }

    @DeleteMapping(path = "/noop-service")
    public String undeployNoopService(@RequestParam(value = "count", defaultValue = "1") int count) {
        int cur;

        cur = 0;
        while (cur < count ) {
            ignite.services().cancelAsync("noop-service-" + cur);
            cur++;
        }

        String msg = "STOPPING " + cur + " NO-OP SERVICE INSTANCES";
        System.out.println(msg);

        return msg;
    }

    private ServiceConfiguration toServiceConfiguration(Workflow workflow) {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        serviceConfiguration.setName(workflow.getUuid());
        serviceConfiguration.setService(new WorkflowService(workflow));
        // Specifying the cache name and key for the affinity based deployment.
        serviceConfiguration.setCacheName("workflows");
        serviceConfiguration.setAffinityKey(workflow.getUuid());
        serviceConfiguration.setTotalCount(1);
        return serviceConfiguration;
    }

    @GetMapping(path = "/load-em-up")
    public void loadEmUp(@RequestParam(value = "size", defaultValue = "SMALL") Network.NetworkSize size) {
        WorkflowGenerator workflowGenerator = new WorkflowGenerator(Network.ofSize(size));
        List<Workflow> workflows = workflowGenerator.getWorkflows();

        // Determine which workflows are already scheduled
        Set<String> existingUuids = ignite.services().serviceDescriptors().stream()
                // Only consider workflow services
                .filter(s -> s.serviceClass().equals(WorkflowService.class))
                .map(ServiceDescriptor::name)
                .collect(Collectors.toSet());

        // Schedule workflows that are not already scheduled
        Set<String> uuids = new HashSet<>();
        for (Workflow workflow : workflows) {
            if (!existingUuids.contains(workflow.getUuid())) {
                ignite.services().deployAsync(toServiceConfiguration(workflow));
            }
            // collect the UUIDs as we iterate
            uuids.add(workflow.getUuid());
        }

        // Un-schedule workflows that we're previously scheduled, and are no longer presentc
        for (String uuid : Sets.difference(existingUuids, uuids)) {
            ignite.services().cancelAsync(uuid);
        }
    }
}
