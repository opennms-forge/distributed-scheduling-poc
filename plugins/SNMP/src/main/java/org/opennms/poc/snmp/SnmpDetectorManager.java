package org.opennms.poc.snmp;

import java.util.function.Consumer;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceDetectorManager;
import org.opennms.poc.plugin.api.ServiceDetectorResults;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;

public class SnmpDetectorManager implements ServiceDetectorManager {

    @Override
    public ServiceDetector create(Consumer<ServiceDetectorResults> resultProcessor) {
        return new SnmpDetector();
    }
}
