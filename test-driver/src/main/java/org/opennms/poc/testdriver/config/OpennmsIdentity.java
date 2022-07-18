package org.opennms.poc.testdriver.config;

import org.opennms.horizon.core.identity.Identity;
import org.opennms.horizon.core.identity.IdentityImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpennmsIdentity {

    @Value("${opennms.identity:test-driver}")
    private String identity;

    @Value("${opennms.location:here}")
    private String location;

    @Bean
    public Identity registerIdentity() {
        return new IdentityImpl(identity, location, "TEST");
    }
}
