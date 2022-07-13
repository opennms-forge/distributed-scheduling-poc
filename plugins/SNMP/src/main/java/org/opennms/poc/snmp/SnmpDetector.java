package org.opennms.poc.snmp;

import java.util.concurrent.CompletableFuture;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceDetectorRequest;
import org.opennms.poc.plugin.api.ServiceDetectorResults;

public class SnmpDetector implements ServiceDetector {

    @Override
    public CompletableFuture<ServiceDetectorResults> detect(ServiceDetectorRequest request) {
        return null;
    }
}
