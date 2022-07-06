package org.opennms.poc.ignite.worker.loadtest.impl;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.services.ServiceConfiguration;
import org.opennms.poc.ignite.worker.loadtest.LoadTestServiceClusterStarter;
import org.opennms.poc.ignite.worker.loadtest.LoadTestServiceStarterJob;
import org.opennms.poc.ignite.worker.loadtest.LoadTestWatcherService;
import org.opennms.poc.ignite.worker.loadtest.PingMethod;

import java.util.LinkedList;
import java.util.List;

public class LoadTestServiceStarterJobImpl implements LoadTestServiceStarterJob {

    private String prefix;
    private int count;
    private LoadTestServiceClusterStarter loadTestServiceClusterStarter;

    @IgniteInstanceResource
    private Ignite ignite;

    @LoggerResource
    private IgniteLogger logger;

    private PingMethod pingMethod;
    private boolean rethrowTaskExceptions = false;

    public LoadTestServiceStarterJobImpl(
            String prefix,
            int count,
            LoadTestServiceClusterStarter loadTestServiceClusterStarter,
            PingMethod pingMethod,
            boolean rethrowTaskExceptions) {

        this.prefix = prefix;
        this.count = count;
        this.loadTestServiceClusterStarter = loadTestServiceClusterStarter;
        this.pingMethod = pingMethod;
        this.rethrowTaskExceptions = rethrowTaskExceptions;
    }

//========================================
// Getters and Setters
//  NOTE: for local use only (not when used as an Ignite Job)
//----------------------------------------

    public IgniteLogger getLogger() {
        return logger;
    }

    public void setLogger(IgniteLogger logger) {
        this.logger = logger;
    }

    public Ignite getIgnite() {
        return ignite;
    }

    public void setIgnite(Ignite ignite) {
        this.ignite = ignite;
    }

//========================================
// Processing
//----------------------------------------

    @Override
    public void run() {
        logger.info("STARTING " + count + " SERVICES WITH PREFIX " + prefix);

        List<String> newServices = new LinkedList<>();
        List<ServiceConfiguration> newServiceConfigurations = new LinkedList<>();

        int cur = 0;
        while (cur < count) {
            String serviceName = String.format("%s-%06d", prefix, cur);

            newServices.add(serviceName);

            ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
            serviceConfiguration.setService(
                    new LoadTestServiceImpl(serviceName, pingMethod, rethrowTaskExceptions)
            );
            serviceConfiguration.setName(serviceName);
            serviceConfiguration.setAffinityKey(serviceName);
            // serviceConfiguration.setCacheName("TBD");
            serviceConfiguration.setTotalCount(1);
            newServiceConfigurations.add(serviceConfiguration);

            cur++;
        }

        ignite
                .services()
                .serviceProxy(LoadTestWatcherService.SERVICE_NAME, LoadTestWatcherService.class, false)
                .servicesAdding(newServices);

        loadTestServiceClusterStarter.startServiceConfiguration(ignite, newServiceConfigurations);
        // ignite.services().deployAllAsync(newServiceConfigurations);
    }
}
