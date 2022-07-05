package org.opennms.poc.ignite.worker.ignite.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.ServiceContextResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;
import org.opennms.poc.ignite.worker.ignite.registries.OsgiServiceHolder;

@Slf4j
//@RequiredArgsConstructor
public class NoopService implements Service {
    private static final long serialVersionUID = 0L;

    @LoggerResource
    private IgniteLogger igniteLogger;

    private final String lastServiceName;

    @ServiceContextResource
    private ServiceContext serviceContext;

    public NoopService(String lastServiceName) {
        this.lastServiceName = lastServiceName;
        log.info("############## Registered Detector count {}", OsgiServiceHolder.getRegisteredDetectorCount());
    }

    @Override
    public void init() throws Exception {
        if (serviceContext.name().equals(lastServiceName)) {
            igniteLogger.info("LAST NO-OP SERVICE INITIALIZED");
        }
    }

    @Override
    public void execute() throws Exception {
        if (serviceContext.name().equals(lastServiceName)) {
            igniteLogger.info("LAST NO-OP SERVICE STARTED");
        }
    }

    @Override
    public void cancel() {
        if (serviceContext.name().equals(lastServiceName)) {
            igniteLogger.info("LAST NO-OP SERVICE STOPPED");
        }
    }
}
