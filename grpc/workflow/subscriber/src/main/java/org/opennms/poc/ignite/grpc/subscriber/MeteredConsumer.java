package org.opennms.poc.ignite.grpc.subscriber;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.opennms.horizon.core.identity.Identity;
import org.opennms.poc.ignite.model.workflows.Results;
import org.opennms.poc.metrics.MetricsProvider;

public class MeteredConsumer implements Consumer<Results>, MetricsProvider {

  private final MetricRegistry metrics = new MetricRegistry();
  private final Identity identity;
  private final Consumer<Results> delegate;

  public MeteredConsumer(Identity identity, Consumer<Results> delegate) {
    this.identity = identity;
    this.delegate = delegate;
  }

  @Override
  public void accept(Results results) {
    Counter counter = get(name("minion", identity.getLocation(), identity.getId(), "result.count"), Counter::new);
    counter.inc();
    delegate.accept(results);
  }

  @Override
  public MetricRegistry getMetrics() {
    return metrics;
  }

  private <T extends Metric> T get(String name, Supplier<T> creator) {
    if (!metrics.getMetrics().containsKey(name)) {
      metrics.register(name, creator.get());
    }
    return (T) metrics.getMetrics().get(name);
  }

}
