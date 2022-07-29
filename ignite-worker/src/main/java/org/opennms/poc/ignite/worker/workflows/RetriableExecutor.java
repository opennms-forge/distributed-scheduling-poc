package org.opennms.poc.ignite.worker.workflows;

import org.opennms.poc.ignite.model.workflows.Workflow;

public interface RetriableExecutor {
    /**
     * Initialize the executor for the workflow.
     *
     * @param workflow workflow definition to execute.
     * @param handleRetryNeeded callback listening for disconnects in order to schedule reconnect attempts.  Only call
     *                         after a successful attempt() call.
     */
    void init(Runnable handleRetryNeeded);

    /**
     * Attempt the executor.  After success, needs to schedule a retry must be triggered by calling the disconnect
     * handler provided at init time.
     *
     * @throws Exception indicate failure of the attempt; another attempt is automatically scheduled.
     */
    void attempt() throws Exception;

    /**
     * Cancel the executor on shutdown of the workflow.
     */
    void cancel();
}
