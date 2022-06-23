package org.opennms.poc.ignite.worker.ignite.service;

import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.ServiceContextResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopService implements Service {
    private static final long serialVersionUID = 0L;

    @LoggerResource
    private IgniteLogger log;

    private final String lastServiceName;

    @ServiceContextResource
    private ServiceContext serviceContext;

    public NoopService(String lastServiceName) {
        this.lastServiceName = lastServiceName;
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
    }

    @Override
    public void cancel() {
        if (serviceContext.name().equals(lastServiceName)) {
            log.info("LAST NO-OP SERVICE STOPPED");
        }
    }
}
