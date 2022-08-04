package org.opennms.poc.ignite.worker.workflows.impl;

import java.util.concurrent.TimeUnit;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.workflows.RetriableExecutor;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutionResultProcessor;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalService;
import org.opennms.poc.scheduler.OpennmsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local implementation of the service to execute a workflow that implements retry handling.  This class runs "locally"
 *  only, so it is never serialized / deserialized; this enables the "ignite" service to be a thin implementation,
 *  reducing the chances of problems due to serialization/deserialization.
 */
public class WorkflowCommonRetryExecutor implements WorkflowExecutorLocalService {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowCommonRetryExecutor.class);

    private Logger log = DEFAULT_LOGGER;

    private OpennmsScheduler opennmsScheduler;

    private Workflow workflow;
    private WorkflowExecutionResultProcessor resultProcessor;
    private RetriableExecutor retriableExecutor;
    private int numRepeatFailures = 0;

    public WorkflowCommonRetryExecutor(
            OpennmsScheduler opennmsScheduler,
            Workflow workflow,
            WorkflowExecutionResultProcessor resultProcessor,
            RetriableExecutor retriableExecutor) {

        this.opennmsScheduler = opennmsScheduler;
        this.workflow = workflow;
        this.resultProcessor = resultProcessor;
        this.retriableExecutor = retriableExecutor;
    }

//========================================
// API
//----------------------------------------

    @Override
    public void start() {
        try {
            retriableExecutor.init(this::handleDisconnect);

            attemptConnect();
        } catch (RuntimeException rtExc) {
            throw rtExc;
        }
    }

    @Override
    public void cancel() {
        opennmsScheduler.cancelTask(workflow.getUuid());
        retriableExecutor.cancel();
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
            retriableExecutor.attempt();
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
}
