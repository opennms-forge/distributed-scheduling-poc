package org.opennms.poc.plugin.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
@Getter
public class FieldConfigMeta {

    private final String annotatedName;
    private final String name;
    private final String javaType;

    @Setter
    private Object value;
    @Setter
    private boolean isEnum;
    @Setter
    private boolean custom;
    @Setter
    private Object[] enumConstants;
}
