package org.opennms.poc.ignite.worker.workflows.impl;

import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalService;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalServiceFactory;
import org.opennms.poc.scheduler.OpennmsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowExecutorLocalServiceFactoryImpl implements WorkflowExecutorLocalServiceFactory {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowExecutorLocalServiceFactoryImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private OpennmsScheduler scheduler;

//========================================
// Constructor
//----------------------------------------

    public WorkflowExecutorLocalServiceFactoryImpl(OpennmsScheduler scheduler) {
        this.scheduler = scheduler;
    }

//========================================
// API
//----------------------------------------

    @Override
    public WorkflowExecutorLocalService create(Workflow workflow) {
        return new WorkflowExecutorLocalServiceImpl(scheduler, workflow);
    }
}
