package org.opennms.poc.ignite.worker.rest;

import org.opennms.poc.ignite.worker.metrics.linux.NetworkInterfaceMetricReporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/stats")
public class StatsRestController {
    @Autowired
    private NetworkInterfaceMetricReporterService networkInterfaceMetricReporterService;

    @GetMapping(value = "/network", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getNetworkStats() {
        Map<String, Map<String, Long>> stats = networkInterfaceMetricReporterService.getCurrentDevStats();

        Map body = formatResponse(stats);

        return ResponseEntity.ok(body);
    }

//========================================
// Internals
//----------------------------------------

    private Map formatResponse(Map<String, Map<String, Long>> stats) {
        Map result = new TreeMap<>();

        Map<String, Long> totals = calculateTotals(stats);

        result.put("totals", totals);
        result.put("devices", stats);

        return result;
    }

    private Map<String, Long> calculateTotals(Map<String, Map<String, Long>> stats) {
        Map<String, Long> result = new TreeMap<>();

        for (Map.Entry<String, Map<String, Long>> oneDevStats : stats.entrySet()) {
            String devName = oneDevStats.getKey();
            Map<String, Long> oneDevAllStats = oneDevStats.getValue();

            for (Map.Entry<String, Long> oneDevOneStat : oneDevAllStats.entrySet()) {
                result.compute(
                        oneDevOneStat.getKey(),
                        (metric, curValue) -> safeAdd(curValue, oneDevOneStat.getValue())
                );
            }
        }

        return result;
    }

    private Long safeAdd(Long first, Long second) {
        long firstValue = 0;
        long secondValue = 0;

        if (first != null) {
            firstValue = first;
        }

        if (second != null) {
            secondValue = second;
        }

        return firstValue + secondValue;
    }
}
