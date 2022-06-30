package org.opennms.poc.ignite.worker.loadtest.impl;

import org.apache.ignite.Ignite;
import org.apache.ignite.services.ServiceConfiguration;
import org.opennms.poc.ignite.worker.loadtest.LoadTestServiceClusterStarter;

import java.util.List;

public class LoadTestServiceClusterBatchStarter implements LoadTestServiceClusterStarter {

    @Override
    public void startServiceConfiguration(Ignite ignite, List<ServiceConfiguration> serviceConfigurationList) {
        ignite.services().deployAllAsync(serviceConfigurationList);
    }
}
