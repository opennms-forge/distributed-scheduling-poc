package org.opennms.poc.ignite.worker.workflows;

import org.opennms.poc.ignite.model.workflows.Workflow;

public interface WorkflowExecutorLocalServiceFactory {
    WorkflowExecutorLocalService create(Workflow workflow);
}
