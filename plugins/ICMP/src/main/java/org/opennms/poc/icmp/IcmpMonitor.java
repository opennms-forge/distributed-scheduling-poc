
package org.opennms.poc.icmp;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.poc.plugin.api.AbstractServiceMonitor;
import org.opennms.poc.plugin.api.MonitoredService;
import org.opennms.poc.plugin.api.ParameterMap;
import org.opennms.poc.plugin.api.PollStatus;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.api.ServiceMonitorResponse.Status;
import org.opennms.poc.plugin.api.ServiceMonitorResponseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcmpMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(IcmpMonitor.class);

    private Supplier<PingerFactory> pingerFactory;

    public IcmpMonitor(PingerFactory pingerFactoryDelegate) {
        pingerFactory = Suppliers.memoize(() -> pingerFactoryDelegate);
    }
    
    @Override
    public CompletableFuture<ServiceMonitorResponse> poll(MonitoredService svc, Map<String, Object> parameters) {
        Number rtt = null;
        InetAddress host = svc.getAddress();

        try {
            
            // get parameters
            //
            int retries = ParameterMap.getKeyedInteger(parameters, "retry", PingConstants.DEFAULT_RETRIES);
            long timeout = ParameterMap.getKeyedLong(parameters, "timeout", PingConstants.DEFAULT_TIMEOUT);
            int packetSize = ParameterMap.getKeyedInteger(parameters, "packet-size", PingConstants.DEFAULT_PACKET_SIZE);
            final int dscp = ParameterMap.getKeyedDecodedInteger(parameters, "dscp", 0);
            final boolean allowFragmentation = ParameterMap.getKeyedBoolean(parameters, "allow-fragmentation", true);

            //TODO: not sure this needs to be async!
            rtt = pingerFactory.get().getInstance(dscp, allowFragmentation).ping(host, timeout, retries,packetSize);
        } catch (Throwable e) {
            LOG.debug("failed to ping {}", host, e);
//            return PollStatus.unavailable(e.getMessage());
            return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.down());
        }
        
        if (rtt != null) {
//            return PollStatus.available(rtt.doubleValue());
            return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.up());

        } else {
            // TODO add a reason code for unavailability
//            return PollStatus.unavailable(null);
            return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.down());

        }

    }

    public void setPingerFactory(PingerFactory pingerFactory) {
        this.pingerFactory = Suppliers.ofInstance(pingerFactory);
    }
}
