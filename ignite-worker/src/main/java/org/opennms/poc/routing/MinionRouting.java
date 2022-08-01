package org.opennms.poc.routing;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;
import org.opennms.horizon.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.horizon.ipc.sink.api.SyncDispatcher;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults;
import org.opennms.poc.plugin.config.PluginMetadata;
import orh.opennms.poc.ignite.grpc.workflow.WorkflowSinkModule;

@Slf4j
public class MinionRouting extends RouteBuilder {

    private final String routeUri;
    public static final String ROUTE_ID =  "MINION_REGISTRATION";
//    private final SyncDispatcher<PluginConfig> dispatcher;


    //TODO: make this configurable
    private final long someDelay=10000;

    public MinionRouting(String uri, MessageDispatcherFactory messageDispatcherFactory) {
        this.routeUri = uri;
//        dispatcher = messageDispatcherFactory.createSyncDispatcher(new WorkflowSinkModule());
    }

    @Override
    public void configure() throws Exception {
        from(routeUri).routeId(ROUTE_ID).
                log(LoggingLevel.INFO, "Got a single plugin config message").
                aggregate(new PluginConfigAggregationStrategy()).constant(true).
                completionTimeout(someDelay).
                process(exchange -> {
                    log.info("Got a plugin registration notice!");

                    List<PluginMetadata> pluginMetadataList = exchange.getIn().getBody(List.class);

                    log.info("Got {} configs", pluginMetadataList.size());

                    // now get the builder for the protobuf message and contruct it from the PluginMetadata

//                    PluginConfig pluginConfig = ????
//                    dispatcher.send(pluginConfig);
                });

        //TODO: we may need dead letter handling here if comms to horizon haven't spun up yet.
    }

    private class PluginConfigAggregationStrategy extends AbstractListAggregationStrategy<PluginMetadata> {

        @Override
        public PluginMetadata getValue(Exchange exchange) {
            return exchange.getIn().getBody(PluginMetadata.class);
        }
    }
}
