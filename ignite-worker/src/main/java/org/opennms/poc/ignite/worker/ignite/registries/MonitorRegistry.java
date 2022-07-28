package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.Map;
import org.opennms.poc.plugin.api.ServiceMonitorManager;

public interface MonitorRegistry {

    ServiceMonitorManager getService(String type);
    int getCount();
    Map<String, ServiceMonitorManager> getServices();
}
