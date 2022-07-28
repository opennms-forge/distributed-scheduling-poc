package org.opennms.poc.icmp;

import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceDetectorRequest;
import org.opennms.poc.plugin.api.ServiceDetectorResults;

@AllArgsConstructor
public class IcmpDetector implements ServiceDetector {
    @Setter
    private String sampleConfig;
    @Override
    public CompletableFuture<ServiceDetectorResults> detect(ServiceDetectorRequest request) {
        return null;
    }
}
