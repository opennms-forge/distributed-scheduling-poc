package org.opennms.poc.plugin.annotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginConfigScanner {

    public List<FieldConfigMeta> getConfigs(Class clazz) {

        Field[] fields = clazz.getDeclaredFields();

        return Arrays.stream(fields).filter(field -> field.getDeclaredAnnotation(HorizonConfig.class) != null).
                map(field -> {
                    FieldConfigMeta fcm =
                        new FieldConfigMeta(field.getName(), field.getType().getName(), "blah");
                    fcm.setEnum(field.getType().isEnum());
                    fcm.setEnumConstants(field.getType().getEnumConstants());
                    HorizonConfig annotation = field.getAnnotation(HorizonConfig.class);
                    fcm.setCustom(annotation.custom());
                    return fcm;
                }).collect(Collectors.toList());
    }


    @ToString
    @RequiredArgsConstructor
    public class FieldConfigMeta {
        private final String name;
        private final String javaType;
        private final String value;

        @Setter
        private boolean isEnum;
        @Setter
        private boolean custom;
        @Setter
        private Object[] enumConstants;
    }
}
