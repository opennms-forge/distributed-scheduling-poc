package org.opennms.poc.ignite.worker.workflows;

import org.opennms.poc.ignite.model.workflows.Workflows;

/**
 * Responsible for management of task lifecycle.
 * Ensure all elements from workflows/task set are processed.
 */
public interface WorkflowLifecycleManager {

  /**
   * Force deployment of a given task set.
   *
   * @param workflows Task set.
   * @return Number of stopped tasks.
   */
  int deploy(Workflows workflows);

}
