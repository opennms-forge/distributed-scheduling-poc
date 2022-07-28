package org.opennms.poc.testdriver.workflow;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.DefaultSettableGauge;
import com.codahale.metrics.MetricRegistry;
import java.time.Duration;
import java.util.Map;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsResultCollector implements ResultCollector {

  private final Logger logger = LoggerFactory.getLogger(MetricsResultCollector.class);
  private final ResultCollector delegate;
  private final MetricRegistry metricRegistry;

  public MetricsResultCollector(ResultCollector delegate, MetricRegistry metricRegistry) {
    this.delegate = delegate;
    this.metricRegistry = metricRegistry;
  }

  @Override
  public void process(WorkflowResult result, Map<String, Object> resultMap) {
    String name = name(getClass(), result.getUuid());

    metricRegistry.counter(name(name, "counter.responses")).inc();

    if (resultMap.containsKey("response.time")) {
      Object responseTime = resultMap.get("response.time");
      if (responseTime instanceof Number) {
        Double pingTime = ((Number) responseTime).doubleValue();
        metricRegistry.timer(name(name, "timer.response.time")).update(Duration.ofMillis(pingTime.longValue()));
      }
    }

    if ("Up".equals(result.getStatus())) {
      metricRegistry.meter(name(name, "up")).mark();
    } else {
      metricRegistry.meter(name(name, "not.up")).mark();
    }

    delegate.process(result, resultMap);
  }

}
