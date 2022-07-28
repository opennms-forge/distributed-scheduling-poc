package org.opennms.poc.testdriver.workflow;

import java.util.Map;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults.WorkflowResult;

public interface ResultCollector {

  void process(WorkflowResult result, Map<String, Object> resultMap);

}
