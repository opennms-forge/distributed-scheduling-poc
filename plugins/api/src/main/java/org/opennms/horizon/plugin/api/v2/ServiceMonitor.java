package org.opennms.horizon.plugin.api.v2;

import java.util.concurrent.CompletableFuture;

public interface ServiceMonitor {

    CompletableFuture<ServiceMonitorResponse> poll(ServiceMonitorRequest request);

}
