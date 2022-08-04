package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.Map;
import org.opennms.poc.plugin.api.ServiceDetectorManager;

public interface DetectorRegistry {

    ServiceDetectorManager getService(String type);
    int getServiceCount();
    Map<String, ServiceDetectorManager> getServices();
}
