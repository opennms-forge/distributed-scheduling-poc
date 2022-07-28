package org.opennms.poc.ignite.worker.ignite.registries;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import com.savoirtech.eos.util.ServiceProperties;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceDetectorManager;
import org.osgi.framework.BundleContext;

@Slf4j
public class DetectorRegistryImpl extends KeyedWhiteboard<String, ServiceDetectorManager> implements DetectorRegistry {

    public static final String PLUGIN_IDENTIFIER = "detector.name";

    public DetectorRegistryImpl(BundleContext bundleContext) {
        super(bundleContext, ServiceDetectorManager.class, (svc, props) -> props.getProperty(PLUGIN_IDENTIFIER));
    }

    @Override
    public int getCount() {
        return getServiceCount();
    }

    @Override
    public Map<String, ServiceDetectorManager> getServices() {
        return super.asMap();
    }

    @Override
    protected String addService(ServiceDetectorManager service, ServiceProperties props) {
        String retVal = super.addService(service, props);

        // TODO: Notify horizon of new plugin registration

        return retVal;
    }
}
