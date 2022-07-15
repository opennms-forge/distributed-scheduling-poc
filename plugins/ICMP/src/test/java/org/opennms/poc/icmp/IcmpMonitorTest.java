package org.opennms.poc.icmp;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.api.ServiceMonitorResponse.Status;

public class IcmpMonitorTest {
    @Mock
    Pinger pinger;
    @Mock
    PingerFactory pingerFactory;

    IcmpMonitor icmpMonitor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(pingerFactory.getInstance()).thenReturn(pinger);
        when(pinger.ping(any())).thenReturn(1);

        icmpMonitor = new IcmpMonitor(pingerFactory);
    }

    @Test
    public void poll() {
        CompletableFuture<ServiceMonitorResponse> response = icmpMonitor.poll(null, null);

        response.whenComplete((completedResponse, exception) -> {

            assertTrue(completedResponse.getStatus().equals(Status.Up));
        });
    }
}