package org.opennms.poc.ignite.worker.ignite.registries;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.services.Service;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.osgi.framework.BundleContext;

@Slf4j
public class MonitorRegistryImpl extends KeyedWhiteboard<String, ServiceMonitor> implements MonitorRegistry, Service {

    public static final String PLUGIN_IDENTIFIER = "monitor.name";

    public MonitorRegistryImpl(BundleContext bundleContext) {
        super(bundleContext, ServiceMonitor.class, (svc, props) -> props.getProperty(PLUGIN_IDENTIFIER));
    }

    @Override
    public int getCount() {
        return getServiceCount();
    }
}
