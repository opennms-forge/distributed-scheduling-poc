package org.opennms.poc.testdriver.controller;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/prometheus")
public class PrometheusWebController {

  private final CollectorRegistry collectorRegistry;

  public PrometheusWebController(CollectorRegistry collectorRegistry) {
    this.collectorRegistry = collectorRegistry;
  }

  @RequestMapping(method = RequestMethod.GET)
  public void getMetrics(HttpServletResponse response) throws Exception {
    Enumeration<MetricFamilySamples> samples = collectorRegistry.metricFamilySamples();

    try (ServletOutputStream outputStream = response.getOutputStream()) {
      try (Writer writer = new OutputStreamWriter(outputStream)) {
        TextFormat.write004(writer, samples);
      }

      outputStream.flush();
    }
  }

}
