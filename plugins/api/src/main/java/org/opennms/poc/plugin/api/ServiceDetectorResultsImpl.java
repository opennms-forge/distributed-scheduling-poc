package org.opennms.poc.plugin.api;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServiceDetectorResultsImpl implements ServiceDetectorResults {
    private boolean serviceDetected;
    private double responseTimeMs;
    private Map<String, String> serviceAttributes;
}
