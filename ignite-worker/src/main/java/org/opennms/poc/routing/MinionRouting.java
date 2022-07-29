package org.opennms.poc.routing;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

@Slf4j
public class MinionRouting extends RouteBuilder {

    public final String routeUri;
    public static final String ROUTE_ID =  "minion:registration";

    public MinionRouting(String uri) {
        this.routeUri = uri;
    }

    @Override
    public void configure() throws Exception {
        from(routeUri).routeId(ROUTE_ID).
        process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                log.info("Got a plugin registration notice!");
                //TODO: send message to horizon
            }
        });

        //TODO: we may need dead letter handling here if comms to horizon haven't spun up yet.
    }
}
