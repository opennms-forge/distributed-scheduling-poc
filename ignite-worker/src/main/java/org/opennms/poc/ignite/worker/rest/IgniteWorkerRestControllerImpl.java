package org.opennms.poc.ignite.worker.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.services.ServiceConfiguration;
import org.apache.ignite.services.ServiceDescriptor;
import org.opennms.poc.ignite.worker.ignite.service.AllRepeatedService;
import org.opennms.poc.ignite.worker.ignite.service.NoopService;
import org.opennms.poc.ignite.worker.ignite.service.WorkflowService;
import org.opennms.poc.ignite.model.workflows.Network;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.workflows.WorkflowGenerator;
import org.opennms.poc.ignite.worker.workflows.WorkflowRepository;

import com.google.common.collect.Sets;

@AllArgsConstructor
@Slf4j
public class IgniteWorkerRestControllerImpl implements IgniteWorkerRestController {

    //TODO: should this be injected through ignite annotation?
    private Ignite ignite;

    private WorkflowRepository workflowRepository;

    @Override
    public void hiOnYoungest() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ignite.compute(
            ignite.cluster().forYoungest())
                .runAsync(() -> {
                    System.out.println("YOUNGEST: hello initiated by node " + myNodeId);
                });
    }

    @Override
    public void hiOnOldest() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ignite.compute(
                ignite.cluster().forOldest())
                .runAsync(
                        () -> {
                            System.out.println("OLDEST: hello initiated by node " + myNodeId);
                });
    }

    @Override
    public void hiAll() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ignite.compute(
                ignite.cluster())
                .broadcastAsync(() -> {
                    System.out.println("ALL: hello initiated by node " + myNodeId);
                });
    }

    @Override
    public void deployHiAllRepeatedService() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        serviceConfiguration.setName("Hi All Repeated");
        serviceConfiguration.setService(new AllRepeatedService(myNodeId));

        serviceConfiguration.setTotalCount(1);
        ignite.services().deploy(serviceConfiguration);
    }

    @Override
    public void removeHiAllRepeatedService() {
        ignite.services().cancel("Hi All Repeated");
    }

    @Override
    public String deployNoopService(int count) {
        int cur;

        String lastServiceName = "noop-service-" + ( count - 1 );

        cur = 0;
        while (cur < count ) {
            String serviceName = "noop-service-" + cur;

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

    @Override
    public String undeployNoopService(int count) {
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

    @Override
    public void loadEmUp(Network.NetworkSize size) {
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

        // Un-schedule workflows that were previously scheduled, and are no longer presentc
        for (String uuid : Sets.difference(existingUuids, uuids)) {
            ignite.services().cancelAsync(uuid);
        }
    }
}
