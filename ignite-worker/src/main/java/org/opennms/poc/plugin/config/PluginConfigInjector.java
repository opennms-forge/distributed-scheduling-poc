package org.opennms.poc.plugin.config;

import java.lang.reflect.Field;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.poc.ignite.worker.ignite.registries.DetectorRegistry;
import org.opennms.poc.plugin.api.ServiceDetector;

@Slf4j
@RequiredArgsConstructor
public class PluginConfigInjector {

//    private final DetectorRegistry detectorRegistry;

//    public void injectConfigs(List<FieldConfigMeta> configs) {
//
//        configs.forEach(config -> {
//            //TODO: need a servicename
//             ServiceDetector serviceDetector = detectorRegistry.getService("???");
//
//             injectConfigs(serviceDetector, config);
//        });
//    }

    public void injectConfigs(Object target, List<FieldConfigMeta> configs) {

        Class clazz = target.getClass();

        configs.forEach(config -> {
            try {
                Field f = clazz.getDeclaredField(config.getName());

                if (config.getJavaType().equals(f.getType().getName())) {
                    f.set(target, config.getValue());
                }
                else {
                    log.error("Field types don't match!");
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
