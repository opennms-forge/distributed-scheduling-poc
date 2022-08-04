package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.opennms.poc.alerting.AlertingService;
import org.opennms.poc.plugin.api.ServiceMonitorManager;
import org.osgi.framework.BundleContext;

@Slf4j
public class MonitorRegistryImpl extends AlertingPluginRegistry<String, ServiceMonitorManager> implements MonitorRegistry {

    public static final String PLUGIN_IDENTIFIER = "monitor.name";

    public MonitorRegistryImpl(BundleContext bundleContext, AlertingService alertingService) {
        super(bundleContext, ServiceMonitorManager.class,PLUGIN_IDENTIFIER, alertingService);
    }

    @Override
    public Map<String, ServiceMonitorManager> getServices() {
        return super.asMap();
    }
}
