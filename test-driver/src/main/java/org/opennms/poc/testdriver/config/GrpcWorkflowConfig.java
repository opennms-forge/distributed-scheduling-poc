package org.opennms.poc.testdriver.config;

import com.codahale.metrics.MetricRegistry;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.poc.ignite.grpc.publisher.internal.GrpcWorkflowPublisher;
import org.opennms.poc.testdriver.workflow.LoggingResultCollector;
import org.opennms.poc.testdriver.workflow.MetricsResultCollector;
import org.opennms.poc.testdriver.workflow.ResultCollector;
import org.opennms.poc.testdriver.workflow.WorkflowManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application Wiring for the GrpcWorkflowPublisher which implements publishing of workflows using the Twin GRPC module
 *  to replicate changes to Minions subscribing to the Workflows feed.
 */
@Configuration
public class GrpcWorkflowConfig {

    @Bean
    @Qualifier("manager")
    WorkflowManager workflowManager(@Qualifier("grpc") GrpcWorkflowPublisher publisher) {
        return new WorkflowManager(publisher);
    }

    @Qualifier("grpc")
    @Bean(initMethod = "start")
    public GrpcWorkflowPublisher prepareWorkflowPublisher(@Autowired TwinPublisher twinPublisher) {
        GrpcWorkflowPublisher result = new GrpcWorkflowPublisher(twinPublisher);

        return result;
    }

    @Bean
    public ResultCollector resultCollector(MetricRegistry metricRegistry) {
        return new MetricsResultCollector(new LoggingResultCollector(), metricRegistry);
    }
}
