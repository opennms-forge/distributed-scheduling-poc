package org.opennms.poc.ignite.worker.workflows.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.services.Service;
import org.opennms.horizon.core.lib.IPAddress;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.ignite.registries.OsgiServiceHolder;
import org.opennms.poc.plugin.api.MonitoredService;
import org.opennms.poc.plugin.api.PollStatus;
import org.opennms.poc.plugin.api.ServiceMonitor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkflowExecutorIgniteService implements Service {

    private Workflow workflow;

    @LoggerResource
    private IgniteLogger logger;

    private transient ScheduledThreadPoolExecutor scheduler;

    public WorkflowExecutorIgniteService(Workflow workflow) {
        this.workflow = workflow;
    }

    @Override
    public void init() throws Exception {
        scheduler = OsgiServiceHolder.getWorkflowScheduledThreadPoolExecutor();
    }

    @Override
    public void execute() throws Exception {
        try {
            long period = Long.parseLong(workflow.getCron());

            scheduler.scheduleAtFixedRate(this::executeIteration, period, period, TimeUnit.MILLISECONDS);
        } catch (Exception exc) {
            // TODO: throttle - we can get very large numbers of these in a short time
            logger.warning("error starting workflow " + workflow.getUuid(), exc);
        }
    }

    @Override
    public void cancel() {

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

    private void executeIteration() {
        try {
            ServiceMonitor monitor = lookupMonitor(workflow);
            if (monitor != null) {
                MonitoredService monitoredService = configureMonitoredService();

                //noinspection unchecked - getParameters() returns Map<String, String>; poll wants Map<String, Object>
                PollStatus pollStatus = monitor.poll(monitoredService, (Map) workflow.getParameters());

                // TBD: REMOVE the json mapping
                logger.info("POLL STATUS: " + new ObjectMapper().writeValueAsString(pollStatus));
            } else {
                logger.info("Skipping service monitor execution; monitor not found: monitor=" + workflow.getType());
            }
        } catch (Exception exc) {
            // TODO: throttle - we can get very large numbers of these in a short time
            logger.warning("error executing workflow " + workflow.getUuid(), exc);
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
