package org.opennms.poc.fakeconnector;

import org.opennms.poc.plugin.api.ServiceConnector;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.api.ServiceMonitorResponseImpl;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class FakeConnector implements ServiceConnector {

    private Consumer<ServiceMonitorResponse> resultProcessor;
    private Map<String, Object> parameters;
    private Runnable disconnectHandler;

    private Timer fireSampleTimer = new Timer();
    private long counter = 0;

    public FakeConnector(Consumer<ServiceMonitorResponse> resultProcessor, Map<String, Object> parameters, Runnable disconnectHandler) {
        this.resultProcessor = resultProcessor;
        this.parameters = parameters;
        this.disconnectHandler = disconnectHandler;
    }

    @Override
    public void connect() throws Exception {
        fireSampleTimer.scheduleAtFixedRate(new FireSampleTask(), 5_000, 5_000);
    }

    @Override
    public void disconnect() {
        fireSampleTimer.cancel();
    }

//========================================
//
//----------------------------------------

    private class FireSampleTask extends TimerTask {
        @Override
        public void run() {
            counter++;

            ServiceMonitorResponse response =
                    ServiceMonitorResponseImpl.builder()
                            .status(ServiceMonitorResponse.Status.Up)
                            .reason("trigger #" + counter)
                            .build()
                    ;

            resultProcessor.accept(response);
        }
    }

}
