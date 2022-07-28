package org.opennms.poc.ignite.grpc.subscriber;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;
import com.google.protobuf.Any;
import com.google.protobuf.NullValue;
import com.google.protobuf.Value;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.opennms.horizon.core.identity.Identity;
import org.opennms.horizon.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.horizon.ipc.sink.api.SyncDispatcher;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults.Builder;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults.WorkflowResult;
import org.opennms.poc.ignite.model.workflows.Result;
import org.opennms.poc.ignite.model.workflows.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orh.opennms.poc.ignite.grpc.workflow.WorkflowSinkModule;

public class MeteredConsumer implements Consumer<Results>, MetricSet {

  private final Identity identity;
  private final Consumer<Results> delegate;
  private Map<String, Metric> metrics;

  public MeteredConsumer(Identity identity, Consumer<Results> delegate) {
    this.identity = identity;
    this.delegate = delegate;
    this.metrics = new LinkedHashMap<>();
  }

  @Override
  public void accept(Results results) {
    Counter counter = get(name("minion", identity.getLocation(), identity.getId(), "result.count"), Counter::new);
    counter.inc();
    delegate.accept(results);
  }

  @Override
  public Map<String, Metric> getMetrics() {
    return metrics;
  }

  private <T extends Metric> T get(String name, Supplier<T> creator) {
    if (!metrics.containsKey(name)) {
      metrics.put(name, creator.get());
    }
    return (T) metrics.get(name);
  }

}
