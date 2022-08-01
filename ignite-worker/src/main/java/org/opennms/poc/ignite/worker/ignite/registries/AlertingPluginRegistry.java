package org.opennms.poc.ignite.worker.ignite.registries;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import com.savoirtech.eos.util.ServiceProperties;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.opennms.poc.ignite.model.workflows.WorkflowType;
import org.opennms.poc.plugin.api.FieldConfigMeta;
import org.opennms.poc.plugin.config.PluginConfigScanner;
import org.opennms.poc.plugin.config.PluginMetadata;
import org.osgi.framework.BundleContext;

@Slf4j
public class AlertingPluginRegistry<K, S> extends KeyedWhiteboard<K, S>  {
    private final ProducerTemplate producerTemplate;
    private final String id;

    public AlertingPluginRegistry(BundleContext bundleContext, Class<S> serviceType, String id, ProducerTemplate producerTemplate) {
        super(bundleContext, serviceType, (svc, props) -> props.getProperty(id));
        this.id = id;
        this.producerTemplate = producerTemplate;
    }

    @Override
    protected K addService(S service, ServiceProperties props) {
        K retVal = super.addService(service, props);

        List<FieldConfigMeta> fieldConfigMetaList = PluginConfigScanner.getConfigs(service.getClass());
        //TODO: make this is the impl class, not the interface
        log.info("################# Performing scan on service {}", service.getClass());
        PluginMetadata pluginMetadata = new PluginMetadata(props.getProperty(id), WorkflowType.DETECTOR, fieldConfigMetaList);
        producerTemplate.sendBody(pluginMetadata);

        return retVal;
    }
}
