package org.opennms.poc.ignite.worker.workflows.impl;

import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutionResultProcessor;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalService;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalServiceFactory;
import org.opennms.poc.plugin.config.PluginConfigInjector;
import org.opennms.poc.scheduler.OpennmsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowExecutorLocalServiceFactoryImpl implements WorkflowExecutorLocalServiceFactory {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowExecutorLocalServiceFactoryImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private OpennmsScheduler scheduler;
    private final PluginConfigInjector pluginConfigInjector;
    private WorkflowExecutionResultProcessor resultProcessor;

//========================================
// Constructor
//----------------------------------------

    public WorkflowExecutorLocalServiceFactoryImpl(
        OpennmsScheduler scheduler,
        WorkflowExecutionResultProcessor resultProcessor,
            PluginConfigInjector pluginConfigInjector) {

        this.scheduler = scheduler;
        this.resultProcessor = resultProcessor;
        this.pluginConfigInjector = pluginConfigInjector;
    }

//========================================
// API
//----------------------------------------

    @Override
    public WorkflowExecutorLocalService create(Workflow workflow) {
        switch (workflow.getType()) {
            case MONITOR:
                return new WorkflowExecutorLocalMonitorServiceImpl(scheduler, workflow, resultProcessor, pluginConfigInjector);

            case LISTENER:
                WorkflowListenerRetriable listenerService = new WorkflowListenerRetriable(workflow, resultProcessor);
                return new WorkflowCommonRetryExecutor(scheduler, workflow, resultProcessor, listenerService);

            case CONNECTOR:
                WorkflowConnectorRetriable connectorService = new WorkflowConnectorRetriable(workflow, resultProcessor);
                return new WorkflowCommonRetryExecutor(scheduler, workflow, resultProcessor, connectorService);

            default:
                throw new RuntimeException("unrecognized workflow type " + workflow.getType());
        }
    }
}
