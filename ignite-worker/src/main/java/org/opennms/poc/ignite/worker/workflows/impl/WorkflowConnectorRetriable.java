package org.opennms.poc.ignite.worker.workflows.impl;

import java.util.HashMap;
import java.util.Map;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.ignite.registries.OsgiServiceHolder;
import org.opennms.poc.ignite.worker.workflows.RetriableExecutor;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutionResultProcessor;
import org.opennms.poc.plugin.api.ServiceConnector;
import org.opennms.poc.plugin.api.ServiceConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector Service
 */
public class WorkflowConnectorRetriable implements RetriableExecutor {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowConnectorRetriable.class);

    private Logger log = DEFAULT_LOGGER;

    private Workflow workflow;
    private WorkflowExecutionResultProcessor resultProcessor;
    private ServiceConnector serviceConnector;

    private Runnable onDisconnect;

    public WorkflowConnectorRetriable(Workflow workflow, WorkflowExecutionResultProcessor resultProcessor) {
        this.workflow = workflow;
        this.resultProcessor = resultProcessor;
    }

//========================================
// API
//----------------------------------------

    @Override
    public void init(Runnable handleRetryNeeded) {
        this.onDisconnect = handleRetryNeeded;
    }

    @Override
    public void attempt() throws Exception {
        ServiceConnectorFactory serviceConnectorFactory = lookupServiceConnectorFactory(workflow);

        Map<String, Object> castMap = new HashMap<>(workflow.getParameters());

        serviceConnector =
                serviceConnectorFactory.create(
                        result -> resultProcessor.queueSendResult(workflow.getUuid(), result),
                        castMap,
                        onDisconnect
                );

        log.info("Attempting to connect: workflow-uuid={}", workflow.getUuid());
        serviceConnector.connect();
    }

    @Override
    public void cancel() {
        serviceConnector.disconnect();
    }

//========================================
// Setup Internals
//----------------------------------------

    private ServiceConnectorFactory lookupServiceConnectorFactory(Workflow workflow) throws Exception {
        String pluginName = workflow.getPluginName();

        ServiceConnectorFactory result = OsgiServiceHolder.getServiceConnectorFactoryRegistry().getService(pluginName);

        if (result == null) {
            log.error("Failed to locate connector factory for workflow: plugin-name={}; workflow-uuid={}",
                    pluginName, workflow.getUuid());
            throw new Exception("Failed to locate connector factory for workflow: plugin-name=" + pluginName);
        }

        return result;
    }
}
