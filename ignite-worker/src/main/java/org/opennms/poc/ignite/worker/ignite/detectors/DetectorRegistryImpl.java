package org.opennms.poc.ignite.worker.ignite.detectors;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceConfiguration;
import org.osgi.framework.BundleContext;

@Slf4j
public class DetectorRegistryImpl extends KeyedWhiteboard<String, Detector> implements DetectorRegistry, Service {

    public static final String IGNITE_SERVICE_NAME = "detectorRegistry";
    public static final String PLUGIN_IDENTIFIER = "detector.name";

    public DetectorRegistryImpl(BundleContext bundleContext, Ignite ignite) {
        super(bundleContext, Detector.class, (svc, props) -> props.getProperty(PLUGIN_IDENTIFIER));
    }

    @Override
    public int getCount() {
        return getServiceCount();
    }
}
