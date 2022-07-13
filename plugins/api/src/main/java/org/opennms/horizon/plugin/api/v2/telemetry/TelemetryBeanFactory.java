package org.opennms.horizon.plugin.api.v2.telemetry;


import org.opennms.horizon.plugin.api.v2.telemetry.model.TelemetryBeanDefinition;

/**
 * The {@link TelemetryBeanFactory} is used to create a {@link TelemetryBeanDefinition}.
 *
 * @param <T> the type of the bean which is created by this factory
 * @param <B> The type of the bean definition which defines the bean to create.
 *
 * @author mvrueden
 */
public interface TelemetryBeanFactory<T, B extends TelemetryBeanDefinition> {
    Class<? extends T> getBeanClass();
    T createBean(B beanDefinition);
}
