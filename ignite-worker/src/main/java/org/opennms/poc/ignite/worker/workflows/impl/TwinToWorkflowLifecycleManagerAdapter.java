package org.opennms.poc.ignite.worker.workflows.impl;

import org.opennms.poc.ignite.grpc.whiteboard.api.MessageListener;
import org.opennms.poc.ignite.model.workflows.Workflows;
import org.opennms.poc.ignite.worker.workflows.WorkflowLifecycleManager;

public class TwinToWorkflowLifecycleManagerAdapter implements MessageListener<Workflows> {

  private final WorkflowLifecycleManager workflowLifecycleManager;

  public TwinToWorkflowLifecycleManagerAdapter(WorkflowLifecycleManager workflowLifecycleManager) {
    this.workflowLifecycleManager = workflowLifecycleManager;
  }

  @Override
  public Class<Workflows> getType() {
    return Workflows.class;
  }

  @Override
  public void accept(Workflows workflows) {
    workflowLifecycleManager.deploy(workflows);
  }
}
