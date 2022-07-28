package org.opennms.poc.icmp;

import java.util.function.Consumer;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.opennms.poc.plugin.api.ServiceMonitorManager;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;

public class IcmpMonitorManager implements ServiceMonitorManager {
    PingerFactory pingerFactory;

    @Override
    public ServiceMonitor create(Consumer<ServiceMonitorResponse> resultProcessor) {
        return new IcmpMonitor(pingerFactory);
    }
}
