package org.opennms.poc.ignite.worker.workflows.impl;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.apache.ignite.Ignite;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceConfiguration;
import org.apache.ignite.services.ServiceDescriptor;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.model.workflows.WorkflowType;
import org.opennms.poc.ignite.model.workflows.Workflows;
import org.opennms.poc.ignite.worker.workflows.WorkflowLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkflowLifecycleManagerImpl implements WorkflowLifecycleManager {

    public static final String SERVICE_NAME_PREFIX = "workflow:";
    public static final String WORKFLOW_SERVICE_CACHE_NAME = "minion.workflow-service";

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowLifecycleManagerImpl.class);

    private Logger log = DEFAULT_LOGGER;

    @Getter
    @Setter
    private Ignite ignite;

//========================================
// Processing
//----------------------------------------

    @Override
    public int deploy(Workflows workflowDefinitions) {

        // Take the snapshot of currently running services.
        Collection<ServiceDescriptor> serviceDescriptorList = ignite.services().serviceDescriptors();

        // Deploy the services that run on every node in the cluster.
        // WARNING: very large numbers of these node singletons will impact startup performance notably due to the
        //  slowness with starting Ignite services one at a time.  Unfortunately, the "node singleton" cannot be started
        //  via deployAllAsync().
        List<String> singletonIds = deployNodeSingletonServices(workflowDefinitions);

        // Prepare the services that run on only 1 node across the cluster
        List<ServiceConfiguration> serviceConfigurationList = prepareOnePerClusterServiceConfigurations(workflowDefinitions);

        // Deploy
        ignite.services().deployAllAsync(serviceConfigurationList);

        // Find the set of services that are no longer needed
        Collection<String> canceledServices =
            calculateServicesToUndeploy(serviceConfigurationList, singletonIds, serviceDescriptorList);
        ignite.services().cancelAllAsync(canceledServices);

        // Log the update summary
        log.info("Completed workflow update: deploy-count={}; cancel-count={}",
            serviceConfigurationList.size() + singletonIds.size(),
            canceledServices.size());
        return canceledServices.size();
    }

    public void close() {
        ignite.close();
    }

//========================================
// Internals
//----------------------------------------

    /**
     * Deploy all of the services for workflow definitions that need to run as Node Singletons (i.e. always 1 running
     *  per node).
     *
     * @param workflowDefinitions
     */
    private List<String> deployNodeSingletonServices(Workflows workflowDefinitions) {
        List<String> deployedServices = new LinkedList<>();

        int count = 0;
        for (Workflow oneWorkflow : workflowDefinitions.getWorkflows()) {
            if (oneWorkflow.getType().equals(WorkflowType.LISTENER)) {
                count++;
                String serviceName = deployOneNodeSingletonService(oneWorkflow);
                deployedServices.add(serviceName);
            }
        }

        return deployedServices;
    }

    private String deployOneNodeSingletonService(Workflow workflow) {
        String serviceName = formulateServiceNameForWorkflow(workflow);
        WorkflowExecutorIgniteService workflowExecutorIgniteService = new WorkflowExecutorIgniteService(workflow);

        ignite.services().deployNodeSingletonAsync(serviceName, workflowExecutorIgniteService);

        return serviceName;
    }

    private List<ServiceConfiguration> prepareOnePerClusterServiceConfigurations(Workflows workflowDefinitions) {
        List<ServiceConfiguration> serviceConfigurationList =
            workflowDefinitions.getWorkflows()
                .stream()
                .filter(this::isWorkflowOnePerCluster)
                .map((workflow) -> {
                        WorkflowExecutorIgniteService workflowExecutorIgniteService = new WorkflowExecutorIgniteService(workflow);
                        ServiceConfiguration serviceConfiguration = prepareServiceConfiguration(workflow, workflowExecutorIgniteService);

                        return serviceConfiguration;
                    }
                )
                .collect(Collectors.toList());

        return serviceConfigurationList;
    }

    private boolean isWorkflowOnePerCluster(Workflow workflow) {
        switch (workflow.getType()) {
            case MONITOR:
            case CONNECTOR:
                return true;

            default:
                return false;
        }
    }

    private String formulateServiceNameForWorkflow(Workflow workflow) {
        return SERVICE_NAME_PREFIX + workflow.getUuid();
    }

    private ServiceConfiguration prepareServiceConfiguration(Workflow workflow, Service service) {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        String serviceName = formulateServiceNameForWorkflow(workflow);

        serviceConfiguration.setName(serviceName);
        serviceConfiguration.setService(service);
        serviceConfiguration.setAffinityKey(workflow.getUuid());
        serviceConfiguration.setCacheName(WORKFLOW_SERVICE_CACHE_NAME);
        serviceConfiguration.setTotalCount(1);

        return serviceConfiguration;
    }

    private Collection<String>
    calculateServicesToUndeploy(
        List<ServiceConfiguration> deployed,
        List<String> deployedSingletonIds,
        Collection<ServiceDescriptor> serviceDescriptorList) {

        Set<String> deployedNames = deployed.stream().map(ServiceConfiguration::getName).collect(Collectors.toSet());
        deployedNames.addAll(deployedSingletonIds);

        Set<String> existingNames =
            serviceDescriptorList.stream().
                map(ServiceDescriptor::name)
                .filter(name -> name.startsWith(SERVICE_NAME_PREFIX))   // Only workflow services
                .collect(Collectors.toSet());

        return Sets.difference(existingNames, deployedNames);
    }
}
