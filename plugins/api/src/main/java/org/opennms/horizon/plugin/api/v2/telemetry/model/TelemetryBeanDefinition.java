package org.opennms.horizon.plugin.api.v2.telemetry.model;
import java.util.Map;

/**
 * The {@link TelemetryBeanDefinition} defines a bean in order to create it afterwards usually via a Factory.
 * It is required in order to allow configuration of beans via a properties file, to for example configure
 * some features and later instantiate the bean accordingly.
 *
 * @author mvrueden
 */
public interface TelemetryBeanDefinition {

    /** The name of the bean */
    String getName();

    /** The type of the bean */
    String getClassName();

    /** Additional parameters for the bean, e.g. to fill setters */
    Map<String, String> getParameterMap();
}
