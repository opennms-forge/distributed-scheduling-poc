package org.opennms.poc.plugin.config;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opennms.poc.plugin.api.annotations.HorizonConfig;

public class PluginConfigScannerTest {

    PluginConfigScanner annotationProcessor;

    @Before
    public void setUp() throws Exception {
        annotationProcessor = new PluginConfigScanner();
    }

    @Test
    public void getConfigs() {
        List<FieldConfigMeta> fieldConfigMeta =  annotationProcessor.getConfigs(TestMinionPlugin.class);
        assertEquals(4,fieldConfigMeta.size());
        fieldConfigMeta.forEach(fieldConfigMeta1 -> System.out.println(fieldConfigMeta1));
    }

    private class TestMinionPlugin {
        @HorizonConfig(name = "blah")
        private String blahString;

        @HorizonConfig(name = "integerField")
        private int blahInt;

        @HorizonConfig(name = "anEnum")
        private HorizonEnum horizonEnum;

        @HorizonConfig(name = "custom", custom = true)
        private MyCustomClass customClass;

        private String notConfigurable;
    }

    private class MyCustomClass {
        String myField;
    }

    private enum HorizonEnum {
        ONE, TWO, THREE
    }
}