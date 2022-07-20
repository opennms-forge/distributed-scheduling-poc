package org.opennms.poc.testdriver.config;

import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class GrpcIpcServerConfig {
    @Bean(destroyMethod = "stopServer")
    public GrpcIpcServer prepareGrpcIpcServer() {
        return new GrpcIpcServerBuilder(new Properties(), 8990, "PT10S");
    }
}
