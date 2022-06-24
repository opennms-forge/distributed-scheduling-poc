package org.opennms.poc.ignite.workflows;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

import java.text.DecimalFormat;
import java.util.List;

import org.junit.Test;
import org.opennms.poc.ignite.worker.workflows.Network;
import org.opennms.poc.ignite.worker.workflows.Workflow;
import org.opennms.poc.ignite.worker.workflows.WorkflowGenerator;
import org.opennms.poc.ignite.worker.workflows.WorkflowRepository;

public class WorkflowTest {

    @Test
    public void canLoadWorkflowFromRepository() {
        WorkflowRepository workflowRepository = new WorkflowRepository();
        assertThat(workflowRepository.getWorkflows(), hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    public void canLoadWorkflowFromGenerator() {
        WorkflowGenerator workflowGenerator = new WorkflowGenerator(Network.SMALL);
        List<Workflow> workflows = workflowGenerator.getWorkflows();
        System.out.println("Total workflows generated: " + workflows.size());
        // Expect at least one workflow per service
        assertThat(workflows, hasSize((greaterThanOrEqualTo((int)(Network.SMALL.getNumServices())))));
    }

    @Test
    public void canPrintNetworkSizes() {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);
        for (Network.NetworkSize size : Network.NetworkSize.values()) {
            Network network = Network.ofSize(size);
            System.out.printf("The %s network has %s nodes, %s interface and %s services.\n", size,
                    decimalFormat.format(network.getNumNodes()),
                    decimalFormat.format(network.getNumInterfaces()),
                    decimalFormat.format(network.getNumServices()));
        }
    }
}
