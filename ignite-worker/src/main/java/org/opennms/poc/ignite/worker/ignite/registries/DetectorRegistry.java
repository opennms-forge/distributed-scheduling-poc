package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.List;
import java.util.Map;
import org.opennms.poc.plugin.api.ServiceDetector;

public interface DetectorRegistry {

    ServiceDetector getService(String type);
    int getCount();
    Map<String, ServiceDetector> getServices();
}
