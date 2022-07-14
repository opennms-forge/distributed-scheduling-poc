package org.opennms.poc.ignite.worker.workflows;

public interface WorkflowExecutorLocalService {
    void start() throws Exception;
    void cancel();
}
