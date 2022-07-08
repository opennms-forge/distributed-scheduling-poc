package org.opennms.poc.ignite.worker.ignite.registries;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.services.Service;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.osgi.framework.BundleContext;

@Slf4j
public class DetectorRegistryImpl extends KeyedWhiteboard<String, ServiceDetector> implements DetectorRegistry, Service {

    public static final String PLUGIN_IDENTIFIER = "detector.name";

    public DetectorRegistryImpl(BundleContext bundleContext) {
        super(bundleContext, ServiceDetector.class, (svc, props) -> props.getProperty(PLUGIN_IDENTIFIER));
    }

    @Override
    public int getCount() {
        return getServiceCount();
    }
}
