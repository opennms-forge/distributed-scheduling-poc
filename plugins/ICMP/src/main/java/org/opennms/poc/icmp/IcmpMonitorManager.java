package org.opennms.poc.icmp;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.poc.plugin.api.ServiceMonitor;
import org.opennms.poc.plugin.api.ServiceMonitorManager;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.api.annotations.HorizonConfig;

@RequiredArgsConstructor
public class IcmpMonitorManager implements ServiceMonitorManager {
    private final PingerFactory pingerFactory;

    @HorizonConfig(displayName = "hypotheticalConfig")
    public String moreConfig;

    @Override
    public ServiceMonitor create(Consumer<ServiceMonitorResponse> resultProcessor) {
        IcmpMonitor icmpMonitor =  new IcmpMonitor(pingerFactory);
        icmpMonitor.setMoreConfig(moreConfig);

        return icmpMonitor;
    }
}
