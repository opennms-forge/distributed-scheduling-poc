package org.opennms.poc.ignite.worker.ignite.registries;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import com.savoirtech.eos.util.ServiceProperties;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleContext;

@Slf4j
public class AlertingPluginRegistry<K, S> extends KeyedWhiteboard<K, S>  {
    public AlertingPluginRegistry(BundleContext bundleContext, Class<S> serviceType, String id) {
        super(bundleContext, serviceType, (svc, props) -> props.getProperty(id));
    }

    @Override
    protected K addService(S service, ServiceProperties props) {
        K retVal = super.addService(service, props);

        // TODO: Notify horizon of new plugin registration

        return retVal;
    }
}
