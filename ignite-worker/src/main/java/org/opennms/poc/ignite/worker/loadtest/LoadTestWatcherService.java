package org.opennms.poc.ignite.worker.loadtest;

import org.apache.ignite.services.Service;

import java.util.List;
import java.util.Map;

public interface LoadTestWatcherService extends Service {
    String SERVICE_NAME = "load-test-watcher-service";

    void servicePing(String serviceName);
    void servicesAdding(List<String> serviceNames);
    Map<String, Long> getPingTimestamps();
}
