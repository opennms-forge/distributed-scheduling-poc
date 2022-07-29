package org.opennms.poc.ignite.worker.workflows.impl;

import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.services.Service;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.ignite.registries.OsgiServiceHolder;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalService;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalServiceFactory;

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
    private transient boolean shutdown;

    private transient Object sync;

    public WorkflowExecutorIgniteService(Workflow workflow) {
        this.workflow = workflow;
    }

//========================================
// Ignite Service API
//----------------------------------------

    @Override
    public void init() throws Exception {
        sync = new Object();
        shutdown = false;

        workflowExecutorLocalServiceFactory = OsgiServiceHolder.getWorkflowExecutorLocalServiceFactory();
    }

    @Override
    public void execute() throws Exception {
        if (shutdown) {
            logger.info("Skipping execution of workflow; appears to have been canceled already");
            return;
        }

        WorkflowExecutorLocalService newLocalService = workflowExecutorLocalServiceFactory.create(workflow);
        synchronized (sync) {
            if (! shutdown) {
                localService = newLocalService;
                localService.start();
            } else {
                logger.info("Aborting execution of workflow; appears to have been canceled before fully started");
            }
        }
    }

    @Override
    public void cancel() {
        WorkflowExecutorLocalService shutdownService = null;

        synchronized (sync) {
            if (! shutdown) {
                shutdownService = localService;
            }
            shutdown = true;
            localService = null;
        }

        if (shutdownService != null) {
            shutdownService.cancel();
        }
    }
}
