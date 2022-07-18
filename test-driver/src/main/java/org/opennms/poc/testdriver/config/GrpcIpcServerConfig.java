package org.opennms.poc.testdriver.config;

import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.poc.testdriver.workaround.WorkaroundGrpcIpcServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class GrpcIpcServerConfig {
    @Bean(destroyMethod = "stopServer")
    public GrpcIpcServer prepareGrpcIpcServer() {
        // TBD888: configuration source for properties - original code used OSGI configuration management
        return new WorkaroundGrpcIpcServerBuilder(new Properties(), 8990, "PT10S");
    }
}
