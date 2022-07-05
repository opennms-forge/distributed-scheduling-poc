package org.opennms.poc.ignite.worker.ignite.detectors;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.osgi.framework.BundleContext;

@Slf4j
public class DetectorRegistryHolder {
    private static DetectorRegistry detectorRegistry;

    public DetectorRegistryHolder(BundleContext bundleContext, Ignite ignite) {
        init(bundleContext, ignite);
    }

    public static void init(BundleContext bundleContext, Ignite ignite) {
        log.info("Creating an instance of the StaticDetectorRegistry for initialization. Don't do this twice!");
        detectorRegistry = new DetectorRegistryImpl(bundleContext, ignite);
    }

    public static Optional<Detector> getDetector(String name) {
         return Optional.ofNullable(detectorRegistry.getService(name));
    }

    public static int getRegisteredDetectorCount() {
        return detectorRegistry.getCount();
    }
}
