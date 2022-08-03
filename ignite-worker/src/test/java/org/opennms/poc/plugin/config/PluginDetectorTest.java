package org.opennms.poc.plugin.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.poc.ignite.model.workflows.PluginMetadata;
import org.opennms.poc.ignite.worker.ignite.registries.DetectorRegistry;
import org.opennms.poc.ignite.worker.ignite.registries.MonitorRegistry;
import org.opennms.poc.plugin.api.MonitoredService;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceDetectorManager;
import org.opennms.poc.plugin.api.ServiceDetectorRequest;
import org.opennms.poc.plugin.api.ServiceDetectorResults;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.opennms.poc.plugin.api.ServiceMonitorManager;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.api.annotations.HorizonConfig;

@Slf4j
public class PluginDetectorTest {
    @Mock
    DetectorRegistry detectorRegistry;
    @Mock
    MonitorRegistry monitorRegistry;

    Map<String, ServiceDetectorManager> serviceDetectorMap;
    Map<String, ServiceMonitorManager> serviceMonitorMap;

    PluginDetector pluginDetector;

    SampleDetectorManager detectorInstance1;
    SampleDetectorManager detectorInstance2;
    SampleMonitorManager monitorInstance1;
    SampleMonitorManager monitorInstance2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        detectorInstance1 = new SampleDetectorManager();
        detectorInstance2 = new SampleDetectorManager();
        monitorInstance1 = new SampleMonitorManager();
        monitorInstance2 = new SampleMonitorManager();
        serviceDetectorMap = Map.of("detectorPlugin1", detectorInstance1, "detectorPlugin2", detectorInstance2);
        when(detectorRegistry.getServices()).thenReturn(serviceDetectorMap);

        serviceMonitorMap = Map.of("monitorPlugin1", monitorInstance1, "monitorPlugin2", monitorInstance2);
        when(monitorRegistry.getServices()).thenReturn(serviceMonitorMap);

        when(detectorRegistry.getService(eq("detectorPlugin1"))).thenReturn(detectorInstance1);
        when(detectorRegistry.getService(eq("detectorPlugin2"))).thenReturn(detectorInstance2);
        when(monitorRegistry.getService(eq("monitorPlugin1"))).thenReturn(monitorInstance1);
        when(monitorRegistry.getService(eq("monitorPlugin2"))).thenReturn(monitorInstance2);

        pluginDetector = new PluginDetector(monitorRegistry, detectorRegistry, new PluginConfigScanner(), new PluginConfigInjector());
    }

    @Test
    public void detect() {

        List<PluginMetadata> pluginMetadataList = pluginDetector.detect();

        assertEquals(4, pluginMetadataList.size());
        pluginMetadataList.forEach(pluginMetadata -> {
            assertNotNull(pluginMetadata.getPluginType());
            assertNotNull(pluginMetadata.getPluginName());
            assertEquals(1,pluginMetadata.getFieldConfigs().size());
            log.info(pluginMetadata.toString());
        });
    }

    @Test
    public void inject() {
        List<PluginMetadata> pluginMetadataList = pluginDetector.detect();

        pluginMetadataList.forEach(pluginMetadata -> {
             pluginMetadata.getFieldConfigs().forEach(config -> {
                 switch (config.getJavaType()) {
                     case  "int":
                         config.setValue(2);
                         break;
                     case "java.lang.String":
                         config.setValue("blahNewString");
                         break;
                         //TODO: incomplete on type.... beanUtils?
                     default:
                 }
             });
        });

        pluginDetector.inject(pluginMetadataList);

        assertEquals("blahNewString", detectorInstance1.someDetectorConfigValue);
    }

    public class SampleDetectorManager implements ServiceDetectorManager {
        @HorizonConfig(displayName="blah")
        @Getter
        @Setter
        //TODO: get this to work on private fields with bean utils?
        public String someDetectorConfigValue;

        @Override
        public ServiceDetector create(Consumer<ServiceDetectorResults> resultProcessor) {
            return new ServiceDetector() {
                @Override
                public CompletableFuture<ServiceDetectorResults> detect(ServiceDetectorRequest request) {
                    return null;
                }
            };
        }
    }

    public class SampleMonitorManager implements ServiceMonitorManager {
        @HorizonConfig(displayName="blah")
        @Getter
        @Setter
        public String someMonitorConfigValue;

        @Override
        public ServiceMonitor create(Consumer<ServiceMonitorResponse> resultProcessor) {
            return new ServiceMonitor() {
                @Override
                public CompletableFuture<ServiceMonitorResponse> poll(MonitoredService svc, Map<String, Object> parameters) {
                    return null;
                }

                @Override
                public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters) {
                    return null;
                }

                @Override
                public String getEffectiveLocation(String location) {
                    return null;
                }
            };
        }
    }
}