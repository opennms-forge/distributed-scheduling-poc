package org.opennms.poc.ignite.worker.workflows;

import org.opennms.poc.plugin.api.ServiceMonitorResponse;

public interface WorkflowExecutionResultProcessor {
    /**
     * Queue the given result to be sent out.
     *
     * @param uuid
     * @param serviceMonitorResponse
     */
    void queueSendResult(String uuid, ServiceMonitorResponse serviceMonitorResponse);
}
