package org.opennms.poc.ignite.worker.workflows;

import org.opennms.poc.ignite.grpc.whiteboard.api.MessageListener;
import org.opennms.poc.ignite.model.workflows.Workflows;

/**
 * Watcher for Workflow definitions responsible to manage worker lifecycles to ensure all Workflows are processed.
 */
public interface WorkflowLifecycleManager extends MessageListener<Workflows> {
}
