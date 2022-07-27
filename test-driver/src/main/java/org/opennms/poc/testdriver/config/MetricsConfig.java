package org.opennms.poc.testdriver.config;

import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.PushGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

  @Value("${push-gateway:push-gateway:8080}")
  private String pushGateway;

  @Bean
  public PushGateway pushGateway() {
    return new PushGateway(pushGateway);
  }

  @Bean
  public MetricRegistry metricRegistry() {
    return new MetricRegistry();
  }

  @Bean
  public CollectorRegistry collectorRegistry(MetricRegistry metricRegistry) {
    CollectorRegistry collectorRegistry = new CollectorRegistry();
    collectorRegistry.register(new DropwizardExports(metricRegistry));
    return collectorRegistry;
  }

}
