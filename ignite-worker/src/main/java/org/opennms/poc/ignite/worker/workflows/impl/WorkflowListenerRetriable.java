package org.opennms.poc.ignite.worker.workflows.impl;

import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.ignite.registries.OsgiServiceHolder;
import org.opennms.poc.ignite.worker.workflows.RetriableExecutor;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutionResultProcessor;
import org.opennms.poc.plugin.api.Listener;
import org.opennms.poc.plugin.api.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Core of the Workflow Executor for LISTENERS which implements the RetriableExecutor, focusing the logic for starting
 *  and maintaining the listener.  Used with WorkflowCommonRetryExecutor for retry handling.
 *
 * NOTE: there currently is no mechanism by which a LISTENER plugin can notify of a lost listener.  If there is a need
 *  to trigger retries, a way for the Listener to notify back of the failure must be added.
 */
public class WorkflowListenerRetriable implements RetriableExecutor {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowListenerRetriable.class);

    private Logger log = DEFAULT_LOGGER;

    private Workflow workflow;
    private Listener listener;
    private WorkflowExecutionResultProcessor resultProcessor;

    private Runnable onDisconnect;

    public WorkflowListenerRetriable(Workflow workflow, WorkflowExecutionResultProcessor resultProcessor) {
        this.workflow = workflow;
        this.resultProcessor = resultProcessor;
    }

//========================================
// API
//----------------------------------------

    @Override
    public void init(Runnable handleRetryNeeded) {
        onDisconnect = handleRetryNeeded;
    }

    @Override
    public void attempt() throws Exception {
        ListenerFactory listenerFactory = lookupListenerFactory(workflow);

        if (listenerFactory != null) {
            log.info("Staring listener: plugin-name={}; workflow-id={}", workflow.getPluginName(), workflow.getUuid());

            Map<String, Object> castMap = new HashMap<>(workflow.getParameters());

            listener = listenerFactory.create(
                    serviceMonitorResponse -> resultProcessor.queueSendResult(workflow.getUuid(),
                            serviceMonitorResponse), castMap);
            listener.start();
        } else {
            log.warn("Listener plugin not registered; workflow will not run: plugin-name={}; workflow-id={}",
                    workflow.getPluginName(), workflow.getUuid());

            throw new Exception("Listener plugin not registered: plugin-name=" + workflow.getPluginName());
        }
    }

    @Override
    public void cancel() {
        if (listener != null) {
            listener.stop();
        }
    }


//========================================
// Setup Internals
//----------------------------------------

    private ListenerFactory lookupListenerFactory(Workflow workflow) {
        String pluginName = workflow.getPluginName();

        ListenerFactory result = OsgiServiceHolder.getListenerFactoryRegistry().getService(pluginName);

        return result;
    }
}
