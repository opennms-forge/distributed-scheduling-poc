package org.opennms.poc.ignite.worker.ignite.service;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.ServiceContextResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;
import org.opennms.poc.ignite.worker.workflows.Workflow;

public class WorkflowService implements Service {
    private static final long serialVersionUID = 0L;

    @LoggerResource
    private IgniteLogger log;

    @IgniteInstanceResource
    private Ignite ignite;

    @ServiceContextResource
    private ServiceContext serviceContext;

    private final Workflow workflow;

    private Timer timer;

    public WorkflowService(Workflow workflow) {
        this.workflow = Objects.requireNonNull(workflow);
    }

    @Override
    public void init() {
        log.info("{} SERVICE INITIALIZED", workflow.getUuid());
    }

    @Override
    public void execute() {
        log.info("{} SERVICE STARTED", workflow.getUuid());
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Executing workflow locally: " + workflow);
                // Notify all the other nodes that the workflow has been executed, for demonstration purposes
                ignite.compute(
                                ignite.cluster())
                        .broadcastAsync(() -> {
                            System.out.printf("(%s,%s): executed at %d\n", workflow.getType(), workflow.getUuid(), System.currentTimeMillis());
                        });
            }
        }, 0, Integer.parseInt(workflow.getCron()));
    }

    @Override
    public void cancel() {
        log.info("{} SERVICE STOPPED", workflow.getUuid());
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
