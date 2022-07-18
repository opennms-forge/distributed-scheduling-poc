package org.opennms.poc.testdriver.config;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.poc.ignite.grpc.publisher.internal.GrpcWorkflowPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application Wiring for the GrpcWorkflowPublisher which implements publishing of workflows using the Twin GRPC module
 *  to replicate changes to Minions subscribing to the Workflows feed.
 */
@Configuration
public class GrpcWorkflowConfig {

    @Bean(initMethod = "start")
    public GrpcWorkflowPublisher prepareWorkflowPublisher(@Autowired TwinPublisher twinPublisher) {
        GrpcWorkflowPublisher result = new GrpcWorkflowPublisher(twinPublisher);

        return result;
    }
}
