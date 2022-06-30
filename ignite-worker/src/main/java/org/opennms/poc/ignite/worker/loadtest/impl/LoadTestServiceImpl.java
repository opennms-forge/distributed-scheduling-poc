package org.opennms.poc.ignite.worker.loadtest.impl;

import org.apache.ignite.Ignite;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.opennms.poc.ignite.worker.loadtest.LoadTestService;
import org.opennms.poc.ignite.worker.loadtest.LoadTestWatcherService;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LoadTestServiceImpl implements LoadTestService {

    public static final int PING_PERIOD = 3_000;

    private static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(20);

    @IgniteInstanceResource
    private Ignite ignite;

    private String serviceName;

    private boolean shutdownInd = false;

    public LoadTestServiceImpl(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void cancel() {
        shutdownInd = true;
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void execute() throws Exception {
        scheduledThreadPoolExecutor.scheduleAtFixedRate(this::periodicPing, PING_PERIOD, PING_PERIOD, TimeUnit.MILLISECONDS);
    }

//========================================
//
//----------------------------------------

    private void periodicPing() {
        if (shutdownInd) {
            throw new RuntimeException("abort");
        }

        ignite.services().serviceProxy(LoadTestWatcherService.SERVICE_NAME, LoadTestWatcherService.class, false).servicePing(serviceName);
    }
}
