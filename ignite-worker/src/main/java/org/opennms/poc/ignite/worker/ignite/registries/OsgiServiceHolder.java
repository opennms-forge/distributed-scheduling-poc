package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.osgi.framework.BundleContext;

@Slf4j
public class OsgiServiceHolder {
    private static DetectorRegistry detectorRegistry;
    private static MonitorRegistry monitorRegistry;

    public OsgiServiceHolder(BundleContext bundleContext) {
        init(bundleContext);
    }

    public static void init(BundleContext bundleContext) {
        log.info("Creating an instance of the StaticDetectorRegistry for initialization. Don't do this twice!");
        detectorRegistry = new DetectorRegistryImpl(bundleContext);
        monitorRegistry = new MonitorRegistryImpl(bundleContext);
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
}
