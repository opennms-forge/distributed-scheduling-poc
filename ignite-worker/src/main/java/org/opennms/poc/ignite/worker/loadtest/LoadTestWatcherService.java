package org.opennms.poc.ignite.worker.loadtest;

import org.apache.ignite.services.Service;

import java.util.List;

public interface LoadTestWatcherService extends Service {
    public static final String SERVICE_NAME = "load-test-watcher-service";

    void servicePing(String serviceName);
    void servicesAdding(List<String> serviceNames);
}
