package org.opennms.poc.ignite.worker.workflows;

import org.opennms.poc.plugin.api.ServiceMonitorResponse;

public interface WorkflowExecutionResultProcessor {
    /**
     * Queue the given result to be sent out.
     *
     * @param serviceMonitorResponse
     */
    void queueSendResult(ServiceMonitorResponse serviceMonitorResponse);
}
