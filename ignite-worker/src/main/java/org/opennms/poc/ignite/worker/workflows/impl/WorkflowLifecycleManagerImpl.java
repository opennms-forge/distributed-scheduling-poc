package org.opennms.poc.ignite.worker.workflows.impl;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.apache.ignite.Ignite;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceConfiguration;
import org.apache.ignite.services.ServiceDescriptor;
import org.opennms.poc.ignite.model.workflows.Workflow;
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
    public Class<Workflows> getType() {
        return Workflows.class;
    }

    @Override
    public void accept(Workflows workflowDefinitions) {

        // Take the snapshot of currently running services.
        Collection<ServiceDescriptor> serviceDescriptorList = ignite.services().serviceDescriptors();

        // Prepare the services to start
        List<ServiceConfiguration> serviceConfigurationList = prepareServiceConfigurations(workflowDefinitions);

        // Deploy
        ignite.services().deployAllAsync(serviceConfigurationList);

        // Find the set of services that are no longer needed
        Collection<String> canceledServices = calculateServicesToUndeploy(serviceConfigurationList, serviceDescriptorList);
        ignite.services().cancelAllAsync(canceledServices);

        // Log the update summary
        log.info("Completed workflow update: deploy-count={}; cancel-count={}",
                serviceConfigurationList.size(),
                canceledServices.size());
    }

//========================================
// Internals
//----------------------------------------

    private List<ServiceConfiguration> prepareServiceConfigurations(Workflows workflowDefinitions) {
        List<ServiceConfiguration> serviceConfigurationList = new LinkedList<>();

        for (Workflow workflow : workflowDefinitions.getWorkflows()) {
            WorkflowExecutorIgniteService workflowExecutorIgniteService = new WorkflowExecutorIgniteService(workflow);
            ServiceConfiguration serviceConfiguration = prepareServiceConfiguration(workflow, workflowExecutorIgniteService);

            serviceConfigurationList.add(serviceConfiguration);
        }

        return serviceConfigurationList;
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
            Collection<ServiceDescriptor> serviceDescriptorList) {

        Set<String> deployedNames = deployed.stream().map(ServiceConfiguration::getName).collect(Collectors.toSet());
        Set<String> existingNames = serviceDescriptorList.stream().map(ServiceDescriptor::name).collect(Collectors.toSet());

        return Sets.difference(existingNames, deployedNames);
    }
}
