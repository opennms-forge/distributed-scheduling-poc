package org.opennms.poc.ignite.worker.workflows.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opennms.horizon.core.lib.IPAddress;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.ignite.registries.OsgiServiceHolder;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalService;
import org.opennms.poc.plugin.api.MonitoredService;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.scheduler.OpennmsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Local implementation of the service to execute a workflow.  This class runs "locally" only, so it is never
 *  serialized / deserialized; this enables the "ignite" service to be a thin implementation, reducing the chances of
 *  problems due to serialization/deserialization.
 */
public class WorkflowExecutorLocalServiceImpl implements WorkflowExecutorLocalService {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowExecutorLocalServiceImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private Workflow workflow;
    private OpennmsScheduler scheduler;

    private AtomicBoolean active = new AtomicBoolean(false);

    public WorkflowExecutorLocalServiceImpl(OpennmsScheduler scheduler, Workflow workflow) {
        this.workflow = workflow;
        this.scheduler = scheduler;

        this.active = new AtomicBoolean(false);
    }

//========================================
// API
//----------------------------------------

    @Override
    public void start() throws Exception {
        try {
            String whenSpec = workflow.getCron().trim();

            // If the value is all digits, use it as periodic time in milliseconds
            if (whenSpec.matches("^\\d+$")) {
                long period = Long.parseLong(workflow.getCron());

                scheduler.schedulePeriodically(workflow.getUuid(), period, TimeUnit.MILLISECONDS, this::executeSerializedIteration);
            } else {
                // Not a number, MUST be a CRON expression
                scheduler.scheduleTaskOnCron(workflow.getUuid(), whenSpec, this::executeSerializedIteration);
            }

        } catch (Exception exc) {
            // TODO: throttle - we can get very large numbers of these in a short time
            log.warn("error starting workflow {}", workflow.getUuid(), exc);
        }
    }

    @Override
    public void cancel() {
        scheduler.cancelTask(workflow.getUuid());
    }


//========================================
// Setup Internals
//----------------------------------------

    private ServiceMonitor lookupMonitor(Workflow workflow) {
        String type = workflow.getType();

        Optional<ServiceMonitor> result = OsgiServiceHolder.getMonitor(type);

        return result.orElse(null);
    }

//========================================
// Processing
//----------------------------------------

    private void executeSerializedIteration() {
        // Verify it's not already active
        if (active.compareAndSet(false, true)) {
            log.trace("Executing iteration of task: workflow-uuid={}", workflow.getUuid());
            executeIteration();
        } else {
            log.debug("Skipping iteration of task as prior iteration is still active: workflow-uuid={}", workflow.getUuid());
        }
    }

    private void executeIteration() {
        try {
            ServiceMonitor monitor = lookupMonitor(workflow);
            if (monitor != null) {
                MonitoredService monitoredService = configureMonitoredService();

                CompletableFuture<ServiceMonitorResponse> future = monitor.poll(monitoredService, (Map) workflow.getParameters());
                future.whenComplete(this::handleExecutionComplete);
            } else {
                log.info("Skipping service monitor execution; monitor not found: monitor=" + workflow.getType());
            }
        } catch (Exception exc) {
            // TODO: throttle - we can get very large numbers of these in a short time
            log.warn("error executing workflow " + workflow.getUuid(), exc);
        }
    }

    private void handleExecutionComplete(ServiceMonitorResponse serviceMonitorResponse, Throwable exc) {
        log.trace("Completed execution: workflow-uuid={}", workflow.getUuid());
        active.set(false);

        if (exc == null) {
            try {
                // TBD: REMOVE the json mapping - feed response back to Core
                log.info("POLL STATUS: " + new ObjectMapper().writeValueAsString(serviceMonitorResponse));
            } catch (JsonProcessingException jpExc) {
                log.warn("error processing workflow response; workflow-uuid=" + workflow.getUuid(), jpExc);
            }
        } else {
            log.warn("error executing workflow; workflow-uuid=" + workflow.getUuid(), exc);
        }
    }

    private MonitoredService configureMonitoredService() throws UnknownHostException {
        String svcName = "TBD";
        String hostname = workflow.getParameters().get("host");

        IPAddress ipAddress = lookupIpAddress(hostname);

        MonitoredService result = new GeneralMonitoredService(svcName, hostname, -1, "TBD", "TBD", ipAddress.toInetAddress());

        return result;
    }

    private IPAddress lookupIpAddress(String hostname) throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(hostname);

        return new IPAddress(inetAddress);
    }
}