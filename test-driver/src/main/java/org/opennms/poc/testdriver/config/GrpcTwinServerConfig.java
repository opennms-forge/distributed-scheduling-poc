package org.opennms.poc.testdriver.config;

import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriber;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriberImpl;
import org.opennms.core.ipc.twin.grpc.publisher.GrpcTwinPublisher;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.horizon.core.identity.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application wiring for the GRPC Twin Publisher which can be used to send Twin updates to subscribers.
 */
@Configuration
public class GrpcTwinServerConfig {
    @Bean(initMethod = "start", destroyMethod = "close")
    public GrpcTwinPublisher prepareGrpcTwinPublisher(@Autowired LocalTwinSubscriber localTwinSubscriber, @Autowired GrpcIpcServer grpcIpcServer) {
        return new GrpcTwinPublisher(localTwinSubscriber, grpcIpcServer);
    }

    @Bean
    public LocalTwinSubscriber prepareLocalTwinSubscriber(@Autowired Identity identity, @Autowired TracerRegistry tracerRegistry) {
        return new LocalTwinSubscriberImpl(identity, tracerRegistry);
    }
}
