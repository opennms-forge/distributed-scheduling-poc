package org.opennms.poc.ignite.workflows;

import java.util.List;

import org.junit.Test;
import org.opennms.poc.ignite.worker.workflows.Workflow;
import org.opennms.poc.ignite.worker.workflows.WorkflowRepository;

public class WorkflowTest {

    @Test
    public void canLoadWorkflows() {
        WorkflowRepository workflowRepository = new WorkflowRepository();
        List<Workflow> workflows = workflowRepository.getWorkflows();
    }
}
