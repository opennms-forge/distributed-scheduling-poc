package org.opennms.poc.ignite.worker.ignite.registries;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import com.savoirtech.eos.util.ServiceProperties;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.opennms.poc.alerting.AlertingService;
import org.opennms.poc.ignite.model.workflows.WorkflowType;
import org.opennms.poc.plugin.api.FieldConfigMeta;
import org.opennms.poc.plugin.config.PluginConfigScanner;
import org.opennms.poc.ignite.model.workflows.PluginMetadata;
import org.osgi.framework.BundleContext;

@Slf4j
public class AlertingPluginRegistry<K, S> extends KeyedWhiteboard<K, S>  {
    private final AlertingService alertingService;

    public AlertingPluginRegistry(BundleContext bundleContext, Class<S> serviceType, String id, AlertingService alertingService) {
        super(bundleContext, serviceType, (svc, props) -> props.getProperty(id));
        this.alertingService = alertingService;
        super.start();
    }

    @Override
    protected K addService(S service, ServiceProperties props) {
        K serviceId = super.addService(service, props);

        if (serviceId != null) {
            List<FieldConfigMeta> fieldConfigMetaList = PluginConfigScanner.getConfigs(service.getClass());
            log.info("Performing scan on service {}", service.getClass());
            PluginMetadata pluginMetadata = new PluginMetadata(serviceId.toString(), WorkflowType.DETECTOR, fieldConfigMetaList);
            alertingService.notifyOfPluginRegistration(pluginMetadata);
        }

        return serviceId;
    }

    @Override
    public void start() {
    }
}
