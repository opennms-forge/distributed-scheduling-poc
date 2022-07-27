package org.opennms.poc.ignite.worker.workflows.impl;

import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.ignite.registries.OsgiServiceHolder;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutionResultProcessor;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalService;
import org.opennms.poc.plugin.api.Listener;
import org.opennms.poc.plugin.api.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Local implementation of the service to execute a Listener workflow.  This class runs "locally" only, so it is never
 *  serialized / deserialized; this enables the "ignite" service to be a thin implementation, reducing the chances of
 *  problems due to serialization/deserialization.
 */
public class WorkflowExecutorLocalListenerServiceImpl implements WorkflowExecutorLocalService {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowExecutorLocalListenerServiceImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private Workflow workflow;
    private Listener listener;
    private WorkflowExecutionResultProcessor resultProcessor;

    public WorkflowExecutorLocalListenerServiceImpl(Workflow workflow, WorkflowExecutionResultProcessor resultProcessor) {
        this.workflow = workflow;
        this.resultProcessor = resultProcessor;
    }

//========================================
// API
//----------------------------------------

    @Override
    public void start() throws Exception {
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
