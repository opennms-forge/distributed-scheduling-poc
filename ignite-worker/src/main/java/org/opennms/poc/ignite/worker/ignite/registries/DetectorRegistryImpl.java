package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.opennms.poc.alerting.AlertingService;
import org.opennms.poc.plugin.api.ServiceDetectorManager;
import org.osgi.framework.BundleContext;

@Slf4j
public class DetectorRegistryImpl extends AlertingPluginRegistry<String, ServiceDetectorManager> implements DetectorRegistry {

    public static final String PLUGIN_IDENTIFIER = "detector.name";

    public DetectorRegistryImpl(BundleContext bundleContext, AlertingService alertingService) {
        super(bundleContext, ServiceDetectorManager.class, PLUGIN_IDENTIFIER, alertingService);
    }

    @Override
    public Map<String, ServiceDetectorManager> getServices() {
        return super.asMap();
    }
}
