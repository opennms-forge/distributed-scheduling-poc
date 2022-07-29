package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.opennms.poc.plugin.api.ServiceDetectorManager;
import org.osgi.framework.BundleContext;

@Slf4j
public class DetectorRegistryImpl extends AlertingPluginRegistry<String, ServiceDetectorManager> implements DetectorRegistry {

    public static final String PLUGIN_IDENTIFIER = "detector.name";

    public DetectorRegistryImpl(BundleContext bundleContext, ProducerTemplate producerTemplate) {
        super(bundleContext, ServiceDetectorManager.class, PLUGIN_IDENTIFIER, producerTemplate);
    }

    @Override
    public Map<String, ServiceDetectorManager> getServices() {
        return super.asMap();
    }
}
