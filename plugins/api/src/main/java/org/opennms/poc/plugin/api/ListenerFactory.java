package org.opennms.poc.plugin.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Interface for a ListenerFactory, which constructs Listeners.
 */
public interface ListenerFactory {
    Listener create(Consumer<ServiceMonitorResponse> resultProcessor, Map<String, Object> parameters);
}
