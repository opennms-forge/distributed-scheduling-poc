package org.opennms.poc.plugin.config;

import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.poc.plugin.api.FieldConfigMeta;

@Slf4j
@RequiredArgsConstructor
public class PluginConfigInjector {
    private final PluginConfigScanner pluginConfigScanner;

    private Gson gson = new Gson();

    public void injectConfigs(Object target, Map<String, String> parameters) {

        Class clazz = target.getClass();
        List<FieldConfigMeta> configs = pluginConfigScanner.getConfigs(clazz);

        if (configs != null) {
            configs.forEach(config -> {
                try {
                    Field f = clazz.getDeclaredField(config.getDeclaredFieldName());
                    String valueToInject = parameters.get(config.getDeclaredFieldName());

                    if (valueToInject!= null && config.getJavaType().equals(f.getType().getName())) {
                        Object convertedValueToInject;
                        if (config.isEnum()) {
                            f.set(target, Enum.valueOf((Class<Enum>) f.getType(), valueToInject));
                        }
                        else {
                            //TODO: incomplete!!! Need a better way other than brute force?
                            switch (config.getJavaType()) {
                                case "int":
                                    convertedValueToInject = Integer.parseInt(valueToInject);
                                    break;
                                case "java.lang.String":
                                    convertedValueToInject = valueToInject;
                                    break;
                                default:
                                    convertedValueToInject = gson.fromJson(valueToInject, Class.forName(config.getJavaType()));
                                    break;
                            }
                            f.set(target, convertedValueToInject);
                        }
                    } else {
                        log.error("Field types don't match!");
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
