package org.opennms.poc.ignite.worker.rest;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.opennms.poc.ignite.worker.workflows.WorkflowRepository;

@AllArgsConstructor
@Slf4j
public class IgniteWorkerRestControllerImpl implements IgniteWorkerRestController {

    //TODO: should this be injected through ignite annotation?
    private Ignite ignite;

    private WorkflowRepository workflowRepository;

    @Override
    public void hiOnYoungest() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ignite.compute(
            ignite.cluster().forYoungest())
                .runAsync(() -> {
                    System.out.println("YOUNGEST: hello initiated by node " + myNodeId);
                });
    }

    @Override
    public void hiOnOldest() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ignite.compute(
                ignite.cluster().forOldest())
                .runAsync(
                        () -> {
                            System.out.println("OLDEST: hello initiated by node " + myNodeId);
                });
    }

    @Override
    public void hiAll() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ignite.compute(
                ignite.cluster())
                .broadcastAsync(() -> {
                    System.out.println("ALL: hello initiated by node " + myNodeId);
                });
    }
}
