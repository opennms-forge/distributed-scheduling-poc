package org.opennms.poc.snmp;

import java.util.function.Consumer;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.opennms.poc.plugin.api.ServiceMonitorManager;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;

public class SnmpMonitorManager implements ServiceMonitorManager {

    @Override
    public ServiceMonitor create(Consumer<ServiceMonitorResponse> resultProcessor) {
        return new SnmpMonitor();
    }
}
