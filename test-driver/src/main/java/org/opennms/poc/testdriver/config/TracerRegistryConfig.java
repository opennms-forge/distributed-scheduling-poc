package org.opennms.poc.testdriver.config;

import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.registry.TracerRegistryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracerRegistryConfig {

    @Bean
    public TracerRegistry prepareTracerRegistry() {
        return new TracerRegistryImpl();
    }
}
