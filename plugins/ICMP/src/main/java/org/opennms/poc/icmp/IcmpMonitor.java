
package org.opennms.poc.icmp;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.net.InetAddress;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.provision.rpc.relocate.ParameterMap;
import org.opennms.poc.plugin.api.AbstractServiceMonitor;
import org.opennms.poc.plugin.api.MonitoredService;
import org.opennms.poc.plugin.api.PollStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class IcmpMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(IcmpMonitor.class);

    private Supplier<PingerFactory> pingerFactory;

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
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

            rtt = pingerFactory.get().getInstance(dscp, allowFragmentation).ping(host, timeout, retries,packetSize);
        } catch (Throwable e) {
            LOG.debug("failed to ping {}", host, e);
            return PollStatus.unavailable(e.getMessage());
        }
        
        if (rtt != null) {
            return PollStatus.available(rtt.doubleValue());
        } else {
            // TODO add a reason code for unavailability
            return PollStatus.unavailable(null);
        }

    }

    public void setPingerFactory(PingerFactory pingerFactory) {
        this.pingerFactory = Suppliers.ofInstance(pingerFactory);
    }
}
