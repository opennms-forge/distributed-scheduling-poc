package org.opennms.poc.ignite.worker.workflows.impl;

import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.services.Service;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.ignite.registries.OsgiServiceHolder;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalService;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalServiceFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ignite version of the service to execute workflows.  Uses the "local" version of the service,
 *  WorkflowExecutorLocalService, which is never serialized/deserialized, reducing the challenges that introduces.
 */
public class WorkflowExecutorIgniteService implements Service {

    private Workflow workflow;

    @LoggerResource
    private IgniteLogger logger;

    private transient WorkflowExecutorLocalServiceFactory workflowExecutorLocalServiceFactory;
    private transient WorkflowExecutorLocalService localService;
    private transient AtomicBoolean active;

    public WorkflowExecutorIgniteService(Workflow workflow) {
        this.workflow = workflow;
    }

//========================================
// Ignite Service API
//----------------------------------------

    @Override
    public void init() throws Exception {
        workflowExecutorLocalServiceFactory = OsgiServiceHolder.getWorkflowExecutorLocalServiceFactory();
        active = new AtomicBoolean(false);
    }

    @Override
    public void execute() throws Exception {
        localService = workflowExecutorLocalServiceFactory.create(workflow);
        localService.start();
    }

    @Override
    public void cancel() {
        localService.cancel();
    }
}
