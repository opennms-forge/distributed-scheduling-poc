package org.opennms.poc.ignite.worker.loadtest;

import org.apache.ignite.Ignite;
import org.apache.ignite.services.ServiceConfiguration;

import java.util.List;

public interface LoadTestServiceClusterStarter {
    void startServiceConfiguration(Ignite ignite, List<ServiceConfiguration> serviceConfigurationList);
}
