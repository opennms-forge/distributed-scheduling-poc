package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.opennms.poc.scheduler.OpennmsScheduler;
import org.osgi.framework.BundleContext;

@Slf4j
public class OsgiServiceHolder {
    private static DetectorRegistry detectorRegistry;
    private static MonitorRegistry monitorRegistry;
    // private static ScheduledThreadPoolExecutor workflowScheduledThreadPoolExecutor;
    private static OpennmsScheduler opennmsScheduler;

    public OsgiServiceHolder(BundleContext bundleContext, OpennmsScheduler opennmsScheduler) {
        init(bundleContext, opennmsScheduler);
    }

    public static void init(BundleContext bundleContext, OpennmsScheduler opennmsScheduler) {
        log.info("Creating an instance of the StaticDetectorRegistry for initialization. Don't do this twice!");
        detectorRegistry = new DetectorRegistryImpl(bundleContext);
        monitorRegistry = new MonitorRegistryImpl(bundleContext);

        OsgiServiceHolder.opennmsScheduler = opennmsScheduler;
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

    // public static ScheduledThreadPoolExecutor getWorkflowScheduledThreadPoolExecutor() {
    //     return workflowScheduledThreadPoolExecutor;
    // }
    //
    // public static void setWorkflowScheduledThreadPoolExecutor(ScheduledThreadPoolExecutor workflowScheduledThreadPoolExecutor) {
    //     OsgiServiceHolder.workflowScheduledThreadPoolExecutor = workflowScheduledThreadPoolExecutor;
    // }

    public static OpennmsScheduler getOpennmsScheduler() {
        return opennmsScheduler;
    }

    public static void setOpennmsScheduler(OpennmsScheduler opennmsScheduler) {
        OsgiServiceHolder.opennmsScheduler = opennmsScheduler;
    }
}
