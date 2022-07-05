package org.opennms.poc.ignite.worker.ignite.registries;

import org.opennms.poc.plugin.api.ServiceDetector;

public interface DetectorRegistry {

    ServiceDetector getService(String type);
    int getCount();
}
