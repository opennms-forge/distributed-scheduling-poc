package org.opennms.poc.ignite.worker.ignite.registries;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import com.savoirtech.eos.util.ServiceProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.osgi.framework.BundleContext;

@Slf4j
public class AlertingPluginRegistry<K, S> extends KeyedWhiteboard<K, S>  {
    ProducerTemplate producerTemplate;

    public AlertingPluginRegistry(BundleContext bundleContext, Class<S> serviceType, String id, ProducerTemplate producerTemplate) {
        super(bundleContext, serviceType, (svc, props) -> props.getProperty(id));
        this.producerTemplate = producerTemplate;
    }

    @Override
    protected K addService(S service, ServiceProperties props) {
        K retVal = super.addService(service, props);

        producerTemplate.sendBodyAndHeaders(null, null);

        return retVal;
    }
}
