package org.opennms.poc.testdriver.workflow;

import java.util.Map;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingResultCollector implements ResultCollector {

  private final Logger logger = LoggerFactory.getLogger(LoggingResultCollector.class);

  @Override
  public void process(WorkflowResult result, Map<String, Object> resultMap) {
    logger.info("Received result workflow={} status={} reason={} params={}", result.getUuid(),
        result.getStatus(), result.getReason(), resultMap);
  }

}
