package org.opennms.poc.plugin.config;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opennms.poc.plugin.api.annotations.HorizonConfig;

public class PluginConfigScannerTest {

    PluginConfigScanner annotationScanner;

    @Before
    public void setUp() throws Exception {
        annotationScanner = new PluginConfigScanner();
    }

    @Test
    public void getConfigs() {
        List<FieldConfigMeta> fieldConfigMeta =  annotationScanner.getConfigs(TestMinionPlugin.class);
        assertEquals(4,fieldConfigMeta.size());
        fieldConfigMeta.forEach(fieldConfigMeta1 -> System.out.println(fieldConfigMeta1));
    }

    private class TestMinionPlugin {
        @HorizonConfig(displayName = "blah")
        public String blahString;

        @HorizonConfig(displayName = "integerField")
        public int blahInt;

        @HorizonConfig(displayName = "anEnum")
        public HorizonEnum horizonEnum;

        @HorizonConfig(displayName = "custom", custom = true)
        public MyCustomClass customClass;

        public String notConfigurable;
    }

    private class MyCustomClass {
        String myField;
    }

    private enum HorizonEnum {
        ONE, TWO, THREE
    }
}