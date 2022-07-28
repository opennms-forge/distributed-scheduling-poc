package org.opennms.poc.plugin.api;

import java.util.function.Consumer;

public interface ServiceMonitorManager {
    ServiceMonitor create(Consumer<ServiceMonitorResponse> resultProcessor);
}
