package org.opennms.poc.alerting;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;
import org.opennms.horizon.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.horizon.ipc.sink.api.SyncDispatcher;
import org.opennms.poc.alerting.proto.FieldConfigMeta;
import org.opennms.poc.alerting.proto.PluginConfigMessage;
import org.opennms.poc.alerting.proto.PluginConfigMessage.Builder;
import org.opennms.poc.alerting.proto.PluginConfigMessage.PluginConfigMeta;
import org.opennms.poc.ignite.model.workflows.PluginMetadata;

@Slf4j
public class MinionRouting extends RouteBuilder {

    private final String routeUri;
    public static final String ROUTE_ID =  "MINION_REGISTRATION";
    private final SyncDispatcher<PluginConfigMessage> dispatcher;


    //TODO: make this configurable
    private final long someDelay=10000;

    public MinionRouting(String uri, MessageDispatcherFactory messageDispatcherFactory) {
        this.routeUri = uri;
        dispatcher = messageDispatcherFactory.createSyncDispatcher(new PluginConfigSinkModule());
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
                    log.info("PluginMetadata {}", pluginMetadataList);

                    // now get the builder for the protobuf message and construct it from the PluginMetadata

                    Builder messageBuilder = PluginConfigMessage.newBuilder();

                    //  iterate over each of the plugins that sent a config
                    pluginMetadataList.forEach(pluginMetadata -> {
                        PluginConfigMeta.Builder pluginConfigMetaBuilder = PluginConfigMeta.newBuilder().
                                setPluginName(pluginMetadata.getPluginName()).
                                setPluginType(pluginMetadata.getPluginType().toString());
                        // iterate over each field in the plugin config
                        pluginMetadata.getFieldConfigs().forEach(fieldConfig -> {
                            FieldConfigMeta.Builder fieldConfigMetaBuilder = FieldConfigMeta.newBuilder().
                                    setJavaType(fieldConfig.getJavaType()).
                                    setIsEnum(fieldConfig.isEnum()).
                                    setCustom(fieldConfig.isCustom()).
                                    setDisplayName(fieldConfig.getDisplayName()).
                                    setDeclaredFieldName(fieldConfig.getDeclaredFieldName());

                                if (fieldConfig.getEnumConstants() != null) {
                                    fieldConfigMetaBuilder.addAllEnumValues((Iterable<String>) Arrays.stream(fieldConfig.getEnumConstants()).iterator());
                                }
                                
                                pluginConfigMetaBuilder.addConfigs(fieldConfigMetaBuilder.build());
                            }
                        );
                        messageBuilder.addPluginconfigs(pluginConfigMetaBuilder.build());
                    });
                    dispatcher.send(messageBuilder.build());
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
