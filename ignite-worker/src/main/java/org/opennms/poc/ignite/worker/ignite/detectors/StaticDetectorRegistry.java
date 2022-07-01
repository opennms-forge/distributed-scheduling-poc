package org.opennms.poc.ignite.worker.ignite.detectors;

import org.apache.ignite.Ignite;
import org.osgi.framework.BundleContext;

public class StaticDetectorRegistry {
    private static DetectorRegistry detectorRegistry;

    public StaticDetectorRegistry(BundleContext bundleContext, Ignite ignite) {
        init(bundleContext, ignite);
    }

    public static void init(BundleContext bundleContext, Ignite ignite) {
        detectorRegistry = new DetectorRegistryImpl(bundleContext, ignite);
    }

    public static Detector getDetector(String name) {
         return detectorRegistry.getService(name);
    }

    public static int getRegisteredDetectorCount() {
        return detectorRegistry.getCount();
    }
}
