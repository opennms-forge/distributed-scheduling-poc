package org.opennms.poc.ignite.worker.loadtest.impl;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.ServiceResource;
import org.opennms.poc.ignite.worker.loadtest.LoadTestService;
import org.opennms.poc.ignite.worker.loadtest.LoadTestWatcherService;
import org.opennms.poc.ignite.worker.loadtest.PingMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LoadTestServiceImpl implements LoadTestService {

    public static final int PING_PERIOD = 3_000;

    private static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(20);

    @IgniteInstanceResource
    private Ignite ignite;

    @ServiceResource(serviceName = LoadTestWatcherService.SERVICE_NAME, proxyInterface = LoadTestWatcherService.class)
    private LoadTestWatcherService watcherService;

    private Logger logger;

    private String serviceName;
    private PingMethod pingMethod;
    private boolean rethrowTaskExceptions = false;

    private boolean shutdownInd = false;

//========================================
// Constructor
//----------------------------------------

    public LoadTestServiceImpl(String serviceName, PingMethod pingMethod, boolean rethrowTaskExceptions) {
        this.serviceName = serviceName;
        this.pingMethod = pingMethod;
        this.rethrowTaskExceptions = rethrowTaskExceptions;
    }

//========================================
// Ignite Service API
//----------------------------------------

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

        try {
            switch (pingMethod) {
                case INJECTED_SERVICE_METHODCALL:
                    watcherService.servicePing(serviceName);
                    break;

                case SERVICE_METHODCALL:
                    // TODO: use the service annotation or save the proxy for multiple uses?
                    ignite.services().serviceProxy(LoadTestWatcherService.SERVICE_NAME, LoadTestWatcherService.class, false).servicePing(serviceName);
                    break;

                case IGNITE_MESSAGE:
                    ignite.message().send("PING", serviceName);
                    break;
            }
        } catch (IgniteException exc) {
            lazyGetLogger().warn("FAILED TO PING FROM SVC {}", serviceName, exc);
            if (rethrowTaskExceptions) {
                // RE-THROW
                throw exc;
            }
        }
    }

    /**
     * Work-around for object serialization/deserialization problems.
     *
     * @return
     */
    private Logger lazyGetLogger() {
        if (logger != null) {
            return logger;
        } else {
            logger = LoggerFactory.getLogger(LoadTestServiceImpl.class);
            return logger;
        }
    }
}
