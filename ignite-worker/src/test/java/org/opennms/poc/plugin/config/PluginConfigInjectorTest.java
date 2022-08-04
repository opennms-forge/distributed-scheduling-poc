package org.opennms.poc.plugin.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.poc.ignite.worker.ignite.registries.DetectorRegistry;
import org.opennms.poc.plugin.api.FieldConfigMeta;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceDetectorManager;
import org.opennms.poc.plugin.api.ServiceDetectorResults;
import org.opennms.poc.plugin.api.annotations.HorizonConfig;

public class PluginConfigInjectorTest  {

    @Mock
    DetectorRegistry detectorRegistry;

    Gson gson = new Gson();

    PluginConfigScanner scanner = new PluginConfigScanner();
    PluginConfigInjector injector = new PluginConfigInjector(scanner);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void injectConfigs() {
        TestMinionPluginManager testMinionPlugin = new TestMinionPluginManager("blahStringValue", 42, HorizonEnum.THREE, new MyCustomClass("blahField"), "notConfirugredValue");
        when(detectorRegistry.getService(anyString())).thenReturn(testMinionPlugin);
        List<FieldConfigMeta> fieldConfigMeta =  scanner.getConfigs(TestMinionPluginManager.class);
        assertEquals(4,fieldConfigMeta.size());
        fieldConfigMeta.forEach(fieldConfigMeta1 -> System.out.println(fieldConfigMeta1));

        MyCustomClass customClass = new MyCustomClass("ewww");
        Map<String, String> values = Map.of("blahString", "newBlahValue", "blahInt", "2", "horizonEnum", "TWO", "customClass", gson.toJson(customClass));

        injector.injectConfigs(testMinionPlugin, values);

        assertTrue(testMinionPlugin.getBlahString().equals("newBlahValue"));
        assertEquals(2, (testMinionPlugin.getBlahInt()));
        assertEquals(HorizonEnum.TWO, testMinionPlugin.getHorizonEnum());
        assertNotNull(testMinionPlugin.getCustomClass());
        assertEquals(customClass, testMinionPlugin.getCustomClass());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class TestMinionPluginManager implements ServiceDetectorManager {
        @HorizonConfig(displayName = "blah")
        public String blahString;

        @HorizonConfig(displayName = "integerField")
        public int blahInt;

        @HorizonConfig(displayName = "anEnum")
        public HorizonEnum horizonEnum;

        @HorizonConfig(displayName = "custom", custom = true)
        public MyCustomClass customClass;

        private String notConfigurable;

        @Override
        public ServiceDetector create(Consumer<ServiceDetectorResults> resultProcessor) {
            return null;
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private class MyCustomClass {
        String myField;
    }

    private enum HorizonEnum {
        ONE, TWO, THREE
    }
}