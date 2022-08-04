package org.opennms.poc.testdriver.config;

import java.util.Arrays;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.grpc.server.GrpcServerConstants;
import org.springframework.beans.factory.annotation.Value;
import org.opennms.core.grpc.interceptor.LoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;


@Configuration
public class GrpcIpcServerConfig {

    public static final long DEFAULT_MAX_MESSAGE_SIZE = 100 * ( 1024 * 1024 );

    @Value("${" + GrpcServerConstants.GRPC_MAX_INBOUND_SIZE + ":" + DEFAULT_MAX_MESSAGE_SIZE + "}")
    private long maxMessageSize;

    @Bean(destroyMethod = "stopServer")
    public GrpcIpcServer prepareGrpcIpcServer() {
        Properties properties = new Properties();
        properties.setProperty(GrpcIpcUtils.GRPC_MAX_INBOUND_SIZE, Long.toString(maxMessageSize));

        return new GrpcIpcServerBuilder(properties, 8990, "PT10S", Arrays.asList(
            new LoggingInterceptor()
        ));
    }
}
