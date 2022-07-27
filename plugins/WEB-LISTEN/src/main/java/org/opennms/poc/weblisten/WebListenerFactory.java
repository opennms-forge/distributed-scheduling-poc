package org.opennms.poc.weblisten;

import org.opennms.poc.plugin.api.Listener;
import org.opennms.poc.plugin.api.ListenerFactory;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

/**
 * PROTOTYPE
 */
public class WebListenerFactory implements ListenerFactory {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WebListenerFactory.class);

    private Logger log = DEFAULT_LOGGER;

    @Override
    public Listener create(Consumer<ServiceMonitorResponse> resultProcessor, Map<String, Object> parameters) {
        WebListener listener = new WebListener(resultProcessor, parameters);

        return listener;
    }
}
