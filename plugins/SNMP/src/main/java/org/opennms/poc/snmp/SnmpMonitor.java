package org.opennms.poc.snmp;

import java.util.Map;
import org.opennms.poc.plugin.api.MonitoredService;
import org.opennms.poc.plugin.api.PollStatus;
import org.opennms.poc.plugin.api.ServiceMonitor;

public class SnmpMonitor implements ServiceMonitor {

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public String getEffectiveLocation(String location) {
        return null;
    }
}