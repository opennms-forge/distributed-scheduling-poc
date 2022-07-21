package org.opennms.poc.ignite.worker.workflows;

public interface WorkflowExecutionResultProcessor {
    /**
     * Queue the given result to be sent out.
     *
     * @param result
     */
    void queueSendResult(Object result);
}
