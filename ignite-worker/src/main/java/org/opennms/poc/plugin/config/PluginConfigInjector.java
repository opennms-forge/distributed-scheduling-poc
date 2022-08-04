package org.opennms.poc.plugin.config;

import java.lang.reflect.Field;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.poc.plugin.api.FieldConfigMeta;

@Slf4j
@RequiredArgsConstructor
public class PluginConfigInjector {

    public static void injectConfigs(Object target, List<FieldConfigMeta> configs) {

        Class clazz = target.getClass();

        if (configs != null) {
            configs.forEach(config -> {
                try {
                    Field f = clazz.getDeclaredField(config.getDeclaredFieldName());

                    if (config.getJavaType().equals(f.getType().getName())) {
                        f.set(target, config.getValue());
                    } else {
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
}
