package org.opennms.poc.plugin.config;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.opennms.poc.plugin.api.FieldConfigMeta;
import org.opennms.poc.plugin.api.annotations.HorizonConfig;

@Slf4j
public class PluginConfigScanner {

    public List<FieldConfigMeta> getConfigs(Class clazz) {

        Field[] fields = clazz.getDeclaredFields();

        return Arrays.stream(fields).filter(field -> field.getDeclaredAnnotation(HorizonConfig.class) != null).
                map(field -> {
                    FieldConfigMeta fcm =
                        new FieldConfigMeta(field.getName(), field.getName(), field.getType().getName());
                    fcm.setEnum(field.getType().isEnum());
                    fcm.setEnumConstants(field.getType().getEnumConstants());
                    HorizonConfig annotation = field.getAnnotation(HorizonConfig.class);
                    fcm.setCustom(annotation.custom());
                    return fcm;
                }).collect(Collectors.toList());
    }

}
