package org.opennms.poc.testdriver.controller;

import static j2html.TagCreator.body;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.title;
import static j2html.TagCreator.tr;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import j2html.tags.DomContent;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.poc.testdriver.view.CodedView;
import org.opennms.poc.testdriver.view.ViewRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/metrics")
public class MetricsWebController {

  private final MetricRegistry metricRegistry;
  private final ViewRegistry viewRegistry;

  public MetricsWebController(ViewRegistry viewRegistry, MetricRegistry metricRegistry) {
    viewRegistry.register("/metrics", "Metrics");
    this.viewRegistry = viewRegistry;
    this.metricRegistry = metricRegistry;
  }

  @RequestMapping(method = RequestMethod.GET)
  ModelAndView getMetrics() {
    return new ModelAndView(CodedView.supplied(() -> {
      return html(
        head(
          title("Metrics"),
          meta().attr("http-equiv", "refresh").attr("content", 5)
        ),
        body(
          viewRegistry.menu(),
          h1("Metrics"),
          h2("Meters"),
          metrics(metricRegistry.getMeters(), ImmutableMap.<String, BiFunction<String, Meter, Object>>builder()
            .put("Id", (id, meter) -> id)
            .put("Count", (id, meter) -> meter.getCount())
            .put("1 min", (id, meter) -> meter.getOneMinuteRate())
            .put("5 min", (id, meter) -> meter.getFiveMinuteRate())
            .put("15 min", (id, meter) -> meter.getFifteenMinuteRate())
            .put("Mean", (id, meter) -> meter.getMeanRate())
            .build()
          ),
          h2("Gauges"),
          metrics(metricRegistry.getGauges(), ImmutableMap.<String, BiFunction<String, Gauge, Object>>builder()
            .put("Id", (id, gauge) -> id)
            .put("Value", (id, gauge) -> gauge.getValue())
            .build()
          ),
          h2("Counters"),
          metrics(metricRegistry.getCounters(), ImmutableMap.<String, BiFunction<String, Counter, Object>>builder()
            .put("Id", (id, counter) -> id)
            .put("Value", (id, counter) -> counter.getCount())
            .build()
          ),
          h2("Histograms"),
          metrics(metricRegistry.getHistograms(), ImmutableMap.<String, BiFunction<String, Histogram, Object>>builder()
            .put("Id", (id, gauge) -> id)
            .put("Median", (id, hgr) -> hgr.getSnapshot().getMedian())
            .put("75thPercentile", (id, hgr) -> hgr.getSnapshot().get75thPercentile())
            .put("95thPercentile", (id, hgr) -> hgr.getSnapshot().get95thPercentile())
            .put("98thPercentile", (id, hgr) -> hgr.getSnapshot().get98thPercentile())
            .put("99thPercentile", (id, hgr) -> hgr.getSnapshot().get99thPercentile())
            .put("999thPercentile", (id, hgr) -> hgr.getSnapshot().get999thPercentile())
            .put("Max", (id, hgr) -> hgr.getSnapshot().getMax())
            .put("Mean", (id, hgr) -> hgr.getSnapshot().getMean())
            .put("Min", (id, hgr) -> hgr.getSnapshot().getMin())
            .put("StdDev", (id, hgr) -> hgr.getSnapshot().getStdDev())
            .build()
          )
        )
      );
    }));
  }

  private <T extends Metric> DomContent metrics(Map<String, T> metrics, Map<String, BiFunction<String, T, Object>> mapping) {
      return table(
        tr(
          each(mapping.keySet(), label -> td(label))
        ),
        each(metrics.entrySet(), metric -> {
          return tr(
            each(mapping.values(), mapper -> td(Objects.toString(
              mapper.apply(metric.getKey(), metric.getValue())
            )))
          );
        })
      ).attr("border", 1).attr("cellspacing", 2).attr("cellpadding", 2).withStyle("width: 100%;");
  }

}
