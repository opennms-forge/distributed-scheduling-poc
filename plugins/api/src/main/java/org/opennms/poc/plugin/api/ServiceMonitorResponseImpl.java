package org.opennms.poc.plugin.api;

import java.util.Map;
import lombok.Builder;

@Builder
public class ServiceMonitorResponseImpl implements ServiceMonitorResponse{
    Status status;

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    public String getReason() {
        return null;
    }

    @Override
    public Map<String, Number> getProperties() {
        return null;
    }

    @Override
    public DeviceConfig getDeviceConfig() {
        return null;
    }

    public static ServiceMonitorResponse unknown() { return builder().status(Status.Unknown).build();}
    public static ServiceMonitorResponse down() { return builder().status(Status.Down).build();}

    public static ServiceMonitorResponse up() { return builder().status(Status.Up).build();}
    public static ServiceMonitorResponse unresponsive() { return builder().status(Status.Unresponsive).build();}


}