package org.opennms.poc.icmp;

import java.util.function.Consumer;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.opennms.poc.plugin.api.ServiceMonitorManager;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.api.annotations.HorizonConfig;

public class IcmpMonitorManager implements ServiceMonitorManager {
    PingerFactory pingerFactory;

    @HorizonConfig(displayName = "hypotheticalConfig")
    public String moreConfig;

    @Override
    public ServiceMonitor create(Consumer<ServiceMonitorResponse> resultProcessor) {
        IcmpMonitor icmpMonitor =  new IcmpMonitor(pingerFactory);
        icmpMonitor.setMoreConfig(moreConfig);

        return icmpMonitor;
    }
}
