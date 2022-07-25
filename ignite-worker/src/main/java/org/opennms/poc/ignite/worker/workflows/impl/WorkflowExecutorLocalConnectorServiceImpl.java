package org.opennms.poc.ignite.worker.workflows.impl;

import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.ignite.registries.OsgiServiceHolder;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutionResultProcessor;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalService;
import org.opennms.poc.plugin.api.ServiceConnector;
import org.opennms.poc.plugin.api.ServiceConnectorFactory;
import org.opennms.poc.scheduler.OpennmsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Local implementation of the service to execute a ServiceConnector workflow.  This class runs "locally" only, so it is
 *  never serialized / deserialized; this enables the "ignite" service to be a thin implementation, reducing the chances
 *  of problems due to serialization/deserialization.
 */
public class WorkflowExecutorLocalConnectorServiceImpl implements WorkflowExecutorLocalService {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowExecutorLocalConnectorServiceImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private OpennmsScheduler opennmsScheduler;

    private Workflow workflow;
    private WorkflowExecutionResultProcessor resultProcessor;
    private ServiceConnector serviceConnector;
    private int numRepeatFailures = 0;

    public WorkflowExecutorLocalConnectorServiceImpl(
            OpennmsScheduler opennmsScheduler,
            Workflow workflow,
            WorkflowExecutionResultProcessor resultProcessor) {

        this.opennmsScheduler = opennmsScheduler;
        this.workflow = workflow;
        this.resultProcessor = resultProcessor;
    }

//========================================
// API
//----------------------------------------

    @Override
    public void start() throws Exception {
        ServiceConnectorFactory serviceConnectorFactory = lookupServiceConnectorFactory(workflow);

        if (serviceConnectorFactory != null) {
            Map<String, Object> castMap = new HashMap<>(workflow.getParameters());

            serviceConnector = serviceConnectorFactory.create(resultProcessor::queueSendResult, castMap, this::handleDisconnect);

            attemptConnect();
        }
    }

    @Override
    public void cancel() {
        opennmsScheduler.cancelTask(workflow.getUuid());
        serviceConnector.disconnect();
    }

//========================================
// Connection Handling
//----------------------------------------

    private void handleDisconnect() {
        scheduleConnectionAttempt();
    }

    private void attemptConnect() {
        try {
            log.info("Attempting to connect: workflow-uuid={}", workflow.getUuid());
            serviceConnector.connect();
            numRepeatFailures = 0;
        } catch (Exception exc) {
            numRepeatFailures++;

            log.info("Failed to connect: workflow-uuid={}", workflow.getUuid(), exc);

            scheduleConnectionAttempt();
        }
    }

    private void scheduleConnectionAttempt() {
        int delay = calculateFallbackDelay();

        log.info("Scheduling next connection attempt: workflow-uuid={}; repeated-failures={}; retry-delay={}",
                workflow.getUuid(),
                numRepeatFailures,
                delay);

        opennmsScheduler.scheduleOnce(workflow.getUuid(), delay, TimeUnit.MILLISECONDS, this::attemptConnect);
    }

    /**
     * Determine the fallback delay before the next connection attempt, which is calculated solely based on the number
     *  of repeated connection failures.
     *
     * Fallback schedule:
     *  0.250 s
     *  1s
     *  5s
     *  10s
     *  30s ...
     *
     * @return
     */
    private int calculateFallbackDelay() {
        switch (numRepeatFailures) {
            case 0: // shouldn't happen
            case 1:
                return 250;

            case 2:
                return 1_000;

            case 3:
                return 5_000;

            case 4:
                return 10_000;

            default:
                return 30_000;
        }
    }

//========================================
// Setup Internals
//----------------------------------------

    private ServiceConnectorFactory lookupServiceConnectorFactory(Workflow workflow) {
        String pluginName = workflow.getPluginName();

        ServiceConnectorFactory result = OsgiServiceHolder.getServiceConnectorFactoryRegistry().getService(pluginName);

        if (result == null) {
            log.error("Failed to locate connector factory for workflow: plugin-name={}; workflow-uuid={}",
                    pluginName, workflow.getUuid());
        }

        return result;
    }
}
