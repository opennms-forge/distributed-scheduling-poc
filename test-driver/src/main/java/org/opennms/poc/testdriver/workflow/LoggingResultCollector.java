package org.opennms.poc.testdriver.workflow;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingResultCollector implements ResultCollector {

  private final Logger logger = LoggerFactory.getLogger(LoggingResultCollector.class);

  private AtomicLong samplesCounted = new AtomicLong(0);


  @Override
  public void process(WorkflowResult result, Map<String, Object> resultMap) {
    long sampleCount = samplesCounted.incrementAndGet();
    if (sampleCount < 20) {
      logger.info("Sample count received " + sampleCount);
      logger.info("Received result workflow={} status={} reason={} params={}", result.getUuid(),
              result.getStatus(), result.getReason(), resultMap);
    } else if ((sampleCount < 1000) && ((sampleCount % 100) == 0)) {
      logger.info("..Sample count received " + sampleCount);
    } else if ((sampleCount < 10000) && ((sampleCount % 1000) == 0)) {
      logger.info("...Sample count received " + sampleCount);
    } else if ((sampleCount % 10000) == 0) {
      logger.info("....Sample count received " + sampleCount);
    }

  }

}
