package org.opennms.poc.ignite.worker.ignite.detectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.osgi.framework.BundleContext;

@Slf4j
public class StaticDetectorRegistry {
    private static DetectorRegistry detectorRegistry;

    public StaticDetectorRegistry(BundleContext bundleContext, Ignite ignite) {
        init(bundleContext, ignite);
    }

    public static void init(BundleContext bundleContext, Ignite ignite) {
        log.info("Creating an instance of the StaticDetectorRegistry for initialization. Don't do this twice!");
        detectorRegistry = new DetectorRegistryImpl(bundleContext, ignite);
    }

    public static Detector getDetector(String name) {
         return detectorRegistry.getService(name);
    }

    public static int getRegisteredDetectorCount() {
        return detectorRegistry.getCount();
    }
}
