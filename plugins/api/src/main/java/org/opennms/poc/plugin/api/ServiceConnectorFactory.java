package org.opennms.poc.plugin.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Interface for a ListenerFactory, which constructs Listeners.
 */
public interface ServiceConnectorFactory {
    /**
     * Create a new connector that will be used to connect to the remote.
     *
     * @param resultProcessor consumer of ServiceMonitorResponse results to be called as samples are received over the
     *                        connection
     * @param parameters configuration for the connector
     * @param disconnectHandler runnable executed on disconnect that enables the Minion to schedule reconnection
     *                          attempts; should only be called on disconnect after a successful connect attempt, and
     *                          not when the connect() method on the ServiceConnector throws an exception.
     * @return
     */
    ServiceConnector create(Consumer<ServiceMonitorResponse> resultProcessor, Map<String, Object> parameters, Runnable disconnectHandler);
}
