package org.opennms.poc.ignite.worker.ignite.detectors;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleContext;

@Slf4j
public class DetectorRegistry extends KeyedWhiteboard<String, Detector> {

    public static final String PLUGIN_IDENTIFIER = "detector.name";

    public DetectorRegistry(BundleContext bundleContext) {
        super(bundleContext, Detector.class, (svc, props) -> props.getProperty(PLUGIN_IDENTIFIER));
    }
}
