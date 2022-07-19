package org.opennms.poc.plugin.api;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServiceMonitorResponseImpl implements ServiceMonitorResponse{
    private Status status;
    private String reason;
    private DeviceConfig deviceConfig;
    private Map<String, Number> properties;

    public static ServiceMonitorResponse unknown() { return builder().status(Status.Unknown).build();}
    public static ServiceMonitorResponse down() { return builder().status(Status.Down).build();}

    public static ServiceMonitorResponse up() { return builder().status(Status.Up).build();}
    public static ServiceMonitorResponse unresponsive() { return builder().status(Status.Unresponsive).build();}


}
