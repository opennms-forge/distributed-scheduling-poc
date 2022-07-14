
package org.opennms.poc.icmp;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.poc.plugin.api.AbstractServiceMonitor;
import org.opennms.poc.plugin.api.MonitoredService;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.api.ServiceMonitorResponseImpl;

public class IcmpMonitor extends AbstractServiceMonitor {

    private Supplier<PingerFactory> pingerFactory;

    public IcmpMonitor(PingerFactory pingerFactoryDelegate) {
        //TODO: setPigerFactory(pingerFactoryDelegate); ????
        pingerFactory = Suppliers.memoize(() -> pingerFactoryDelegate);
    }

    @Override
    public CompletableFuture<ServiceMonitorResponse> poll(MonitoredService svc, Map<String, Object> parameters) {

        Pinger pinger = pingerFactory.get().getInstance();
        CompletableFuture<ServiceMonitorResponse> future = new CompletableFuture<>();
        try {
            pinger.ping(null, 1, 2, 3, new PingResponseCallback() {
                @Override
                public void handleResponse(InetAddress inetAddress, EchoPacket response) {
                    double responseTimeMicros = Math.round(response.elapsedTime(TimeUnit.MICROSECONDS));
                    //TODO: set the responseTimeMicros into the response properties?
                    future.complete(ServiceMonitorResponseImpl.up());
                }

                @Override
                public void handleTimeout(InetAddress inetAddress, EchoPacket echoPacket) {
                    future.complete(ServiceMonitorResponseImpl.unknown());
                }

                @Override
                public void handleError(InetAddress inetAddress, EchoPacket echoPacket, Throwable throwable) {
                    future.complete(ServiceMonitorResponseImpl.down());
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;

    }

    public void setPingerFactory(PingerFactory pingerFactory) {
        this.pingerFactory = Suppliers.ofInstance(pingerFactory);
    }
}
