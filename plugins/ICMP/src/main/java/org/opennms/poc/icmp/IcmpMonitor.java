
package org.opennms.poc.icmp;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.poc.plugin.api.AbstractServiceMonitor;
import org.opennms.poc.plugin.api.MonitoredService;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.api.ServiceMonitorResponse.Status;
import org.opennms.poc.plugin.api.ServiceMonitorResponseImpl;

@RequiredArgsConstructor
public class IcmpMonitor extends AbstractServiceMonitor {

    private final PingerFactory pingerFactory;
    //TODO: double check the constant for this key, probabloy defined elsewhere already
    public final static String RESPONSE_TIME = "response.time";

    @Override
    public CompletableFuture<ServiceMonitorResponse> poll(MonitoredService svc, Map<String, Object> parameters) {

        Pinger pinger = pingerFactory.getInstance(0, true);
        CompletableFuture<ServiceMonitorResponse> future = new CompletableFuture<>();
        try {
            pinger.ping(svc.getAddress(), 1, 2, 3, new PingResponseCallback() {
                @Override
                public void handleResponse(InetAddress inetAddress, EchoPacket response) {
                    double responseTimeMicros = Math.round(response.elapsedTime(TimeUnit.MICROSECONDS));
                    future.complete(ServiceMonitorResponseImpl.builder().status(Status.Up).properties(Map.of(RESPONSE_TIME, responseTimeMicros)).build());
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
}
