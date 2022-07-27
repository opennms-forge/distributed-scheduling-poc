package org.opennms.poc.ignite.worker.ignite.registries;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.osgi.framework.BundleContext;

@Slf4j
public class DetectorRegistryImpl extends KeyedWhiteboard<String, ServiceDetector> implements DetectorRegistry {

    public static final String PLUGIN_IDENTIFIER = "detector.name";

    public DetectorRegistryImpl(BundleContext bundleContext) {
        super(bundleContext, ServiceDetector.class, (svc, props) -> props.getProperty(PLUGIN_IDENTIFIER));
    }

    @Override
    public int getCount() {
        return getServiceCount();
    }

    @Override
    public Map<String, ServiceDetector> getServices() {
        return super.asMap();
    }
}
