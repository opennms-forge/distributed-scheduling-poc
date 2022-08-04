package org.opennms.poc.plugin.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.opennms.poc.ignite.model.workflows.PluginMetadata;
import org.opennms.poc.ignite.model.workflows.WorkflowType;
import org.opennms.poc.ignite.worker.ignite.registries.DetectorRegistry;
import org.opennms.poc.ignite.worker.ignite.registries.MonitorRegistry;
import org.opennms.poc.plugin.api.ServiceDetectorManager;
import org.opennms.poc.plugin.api.ServiceMonitorManager;

@AllArgsConstructor
@Deprecated
public class PluginDetector {

    private final MonitorRegistry monitorRegistry;
    private final DetectorRegistry detectorRegistry;
    private final PluginConfigScanner pluginConfigScanner;
    private final PluginConfigInjector pluginConfigInjector;

    public List<PluginMetadata> detect() {
        List<PluginMetadata> detectedPlugins = new ArrayList<>();
         Map<String, ServiceDetectorManager> detectorPlugins = detectorRegistry.getServices();

         detectorPlugins.forEach((name, plugin) -> {
             detectedPlugins.add(new PluginMetadata(name, WorkflowType.DETECTOR, pluginConfigScanner.getConfigs(plugin.getClass())));
         });

         Map<String, ServiceMonitorManager> monitorPlugins = monitorRegistry.getServices();

         monitorPlugins.forEach((name, plugin) -> {
              detectedPlugins.add(new PluginMetadata(name, WorkflowType.CONNECTOR, pluginConfigScanner.getConfigs(plugin.getClass())));
         });
         
         return detectedPlugins;
    }

    public void inject(List<PluginMetadata> pluginMetadataList) {
        pluginMetadataList.forEach(pluginMetadata -> {
            Object target = null;
            switch (pluginMetadata.getPluginType()) {
                case DETECTOR:
                    target = detectorRegistry.getService(pluginMetadata.getPluginName());
                    break;
                case MONITOR:
                    target = monitorRegistry.getService(pluginMetadata.getPluginName());
                    break;
                case LISTENER:
                    break;
                case CONNECTOR:
                default:
                    break;

            }
            if (target != null) {
                // blech!!!
                Object finalTarget = target;
                pluginConfigInjector.injectConfigs(finalTarget, null);
            }
        });
    }

}
