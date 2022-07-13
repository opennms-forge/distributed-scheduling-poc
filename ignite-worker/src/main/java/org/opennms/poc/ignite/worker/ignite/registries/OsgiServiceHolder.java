package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.osgi.framework.BundleContext;

@Slf4j
public class OsgiServiceHolder {
    private static DetectorRegistry detectorRegistry;
    private static MonitorRegistry monitorRegistry;
    private static ScheduledThreadPoolExecutor workflowScheduledThreadPoolExecutor;

    public OsgiServiceHolder(BundleContext bundleContext, ScheduledThreadPoolExecutor workflowScheduledThreadPoolExecutor) {
        init(bundleContext, workflowScheduledThreadPoolExecutor);
    }

    public static void init(BundleContext bundleContext, ScheduledThreadPoolExecutor workflowScheduledThreadPoolExecutor) {
        log.info("Creating an instance of the StaticDetectorRegistry for initialization. Don't do this twice!");
        detectorRegistry = new DetectorRegistryImpl(bundleContext);
        monitorRegistry = new MonitorRegistryImpl(bundleContext);

        OsgiServiceHolder.workflowScheduledThreadPoolExecutor = workflowScheduledThreadPoolExecutor;
    }

    public static Optional<ServiceDetector> getDetector(String name) {
         return Optional.ofNullable(detectorRegistry.getService(name));
    }

    public static int getRegisteredDetectorCount() {
        return detectorRegistry.getCount();
    }

    public static Optional<ServiceMonitor> getMonitor(String name) {
        return Optional.ofNullable(monitorRegistry.getService(name));
    }

    public static int getRegisteredMonitorCount() {
        return monitorRegistry.getCount();
    }

    public static ScheduledThreadPoolExecutor getWorkflowScheduledThreadPoolExecutor() {
        return workflowScheduledThreadPoolExecutor;
    }

    public static void setWorkflowScheduledThreadPoolExecutor(ScheduledThreadPoolExecutor workflowScheduledThreadPoolExecutor) {
        OsgiServiceHolder.workflowScheduledThreadPoolExecutor = workflowScheduledThreadPoolExecutor;
    }
}
