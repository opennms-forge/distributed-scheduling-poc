package org.opennms.poc.ignite.worker.ignite.registries;

import org.opennms.poc.plugin.api.ServiceMonitor;

public interface MonitorRegistry {

    ServiceMonitor getService(String type);
    int getCount();
}
