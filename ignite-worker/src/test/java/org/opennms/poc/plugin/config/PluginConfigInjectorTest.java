package org.opennms.poc.plugin.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.poc.ignite.worker.ignite.registries.DetectorRegistry;
import org.opennms.poc.plugin.api.annotations.HorizonConfig;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceDetectorRequest;
import org.opennms.poc.plugin.api.ServiceDetectorResults;

public class PluginConfigInjectorTest  {

    PluginConfigScanner annotationProcessor;
    PluginConfigInjector pluginConfigInjector;

    @Mock
    DetectorRegistry detectorRegistry;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        annotationProcessor = new PluginConfigScanner();
        pluginConfigInjector = new PluginConfigInjector();
    }

    @Test
    public void injectConfigs() {
        TestMinionPlugin testMinionPlugin = new TestMinionPlugin("blahStringValue", 42, HorizonEnum.THREE, new MyCustomClass("blahField"), "notConfirugredValue");
        when(detectorRegistry.getService(anyString())).thenReturn(testMinionPlugin);
        List<FieldConfigMeta> fieldConfigMeta =  annotationProcessor.getConfigs(TestMinionPlugin.class);
        assertEquals(4,fieldConfigMeta.size());
        fieldConfigMeta.forEach(fieldConfigMeta1 -> System.out.println(fieldConfigMeta1));

        FieldConfigMeta fieldConfigMetaBlahString = new FieldConfigMeta("blah", "blahString","java.lang.String");
        fieldConfigMetaBlahString.setValue("newBlahValue");
        FieldConfigMeta fieldConfigMetaBlahInt = new FieldConfigMeta("integerField", "blahInt","int");
        fieldConfigMetaBlahInt.setValue(Integer.valueOf(2));
        FieldConfigMeta fieldConfigMetaEnum = new FieldConfigMeta("anEnum", "horizonEnum","org.opennms.poc.plugin.config.PluginConfigInjectorTest$HorizonEnum");
        fieldConfigMetaEnum.setValue(HorizonEnum.TWO);
        MyCustomClass customClass = new MyCustomClass("ewww");
        FieldConfigMeta fieldConfigMetaCustom = new FieldConfigMeta("custom", "customClass","org.opennms.poc.plugin.config.PluginConfigInjectorTest$MyCustomClass");
        fieldConfigMetaCustom.setValue(customClass);


        pluginConfigInjector.injectConfigs( testMinionPlugin, Arrays.asList(fieldConfigMetaBlahString, fieldConfigMetaBlahInt, fieldConfigMetaEnum, fieldConfigMetaCustom));

        assertTrue(testMinionPlugin.getBlahString().equals("newBlahValue"));
        assertEquals(2, (testMinionPlugin.getBlahInt()));
        assertEquals(HorizonEnum.TWO, testMinionPlugin.getHorizonEnum());
        assertNotNull(testMinionPlugin.getCustomClass());
        assertEquals(customClass, testMinionPlugin.getCustomClass());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class TestMinionPlugin implements ServiceDetector {
        @HorizonConfig(name = "blah")
        public String blahString;

        @HorizonConfig(name = "integerField")
        public int blahInt;

        @HorizonConfig(name = "anEnum")
        public HorizonEnum horizonEnum;

        @HorizonConfig(name = "custom", custom = true)
        public MyCustomClass customClass;

        private String notConfigurable;

        @Override
        public CompletableFuture<ServiceDetectorResults> detect(ServiceDetectorRequest request) {
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