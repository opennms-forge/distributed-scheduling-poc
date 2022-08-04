package org.opennms.poc.ignite.worker.workflows.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.opennms.horizon.core.lib.IPAddress;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.worker.ignite.registries.OsgiServiceHolder;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutionResultProcessor;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalService;
import org.opennms.poc.plugin.api.MonitoredService;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.opennms.poc.plugin.api.ServiceMonitorManager;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.config.PluginConfigInjector;
import org.opennms.poc.scheduler.OpennmsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local implementation of the service to execute a Monitor workflow.  This class runs "locally" only, so it is never
 *  serialized / deserialized; this enables the "ignite" service to be a thin implementation, reducing the chances of
 *  problems due to serialization/deserialization.
 */
public class WorkflowExecutorLocalMonitorServiceImpl implements WorkflowExecutorLocalService {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowExecutorLocalMonitorServiceImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private Workflow workflow;
    private OpennmsScheduler scheduler;
    private WorkflowExecutionResultProcessor resultProcessor;
    private final PluginConfigInjector pluginConfigInjector;
    private ServiceMonitor monitor=null;

    private AtomicBoolean active = new AtomicBoolean(false);

    public WorkflowExecutorLocalMonitorServiceImpl(OpennmsScheduler scheduler, Workflow workflow,
        WorkflowExecutionResultProcessor resultProcessor, PluginConfigInjector pluginConfigInjector) {
        this.workflow = workflow;
        this.scheduler = scheduler;
        this.resultProcessor = resultProcessor;
        this.pluginConfigInjector = pluginConfigInjector;
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
                // Not a number, REQUIRED to be a CRON expression
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

    private Optional<ServiceMonitor> lookupMonitor(Workflow workflow) {
        String pluginName = workflow.getPluginName();

        Optional<ServiceMonitorManager> result = OsgiServiceHolder.getMonitorManager(pluginName);

        if (result.isPresent()) {
            ServiceMonitorManager foundMonitorManager = result.get();

            pluginConfigInjector.injectConfigs(foundMonitorManager, workflow.getParameters());

            //TODO: what parameters (if any) to pass on creation? Probably none since we want to inject everything from schema.
            return Optional.of(foundMonitorManager.create(null));
        }
        else return Optional.empty();
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
            if (monitor == null) {
                Optional<ServiceMonitor> lazyMonitor = lookupMonitor(workflow);
                if (lazyMonitor.isPresent()) {
                    this.monitor = lazyMonitor.get();
                }
            }
            if (monitor != null) {
                MonitoredService monitoredService = configureMonitoredService();

                Map<String, Object> castMap = new HashMap<>(workflow.getParameters());
                CompletableFuture<ServiceMonitorResponse> future = monitor.poll(monitoredService, castMap);
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
            resultProcessor.queueSendResult(workflow.getUuid(), serviceMonitorResponse);
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
