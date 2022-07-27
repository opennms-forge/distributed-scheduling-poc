package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.List;
import java.util.Map;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceMonitor;

public interface MonitorRegistry {

    ServiceMonitor getService(String type);
    int getCount();
    Map<String, ServiceMonitor> getServices();
}
