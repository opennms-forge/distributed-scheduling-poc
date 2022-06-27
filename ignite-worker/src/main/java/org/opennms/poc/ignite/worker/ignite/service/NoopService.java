package org.opennms.poc.ignite.worker.ignite.service;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCountDownLatch;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.ServiceContextResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;

public class NoopService implements Service {
    private static final long serialVersionUID = 0L;

    @LoggerResource
    private IgniteLogger log;

    private final String lastServiceName;
    private final String latchName;

    @ServiceContextResource
    private ServiceContext serviceContext;

    @IgniteInstanceResource
    private Ignite ignite;

    public NoopService(String lastServiceName, String latchName) {
        this.lastServiceName = lastServiceName;
        this.latchName = latchName;
    }

    @Override
    public void init() throws Exception {
        if (serviceContext.name().equals(lastServiceName)) {
            log.info("LAST NO-OP SERVICE INITIALIZED");
        }
    }

    @Override
    public void execute() throws Exception {
        if (serviceContext.name().equals(lastServiceName)) {
            log.info("LAST NO-OP SERVICE STARTED");
        }

        IgniteCountDownLatch latch = ignite.countDownLatch(latchName, -1, false, false);
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public void cancel() {
        if (serviceContext.name().equals(lastServiceName)) {
            log.info("LAST NO-OP SERVICE STOPPED");
        }
    }
}
