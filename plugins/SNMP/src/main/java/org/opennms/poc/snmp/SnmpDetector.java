package org.opennms.poc.snmp;

import org.opennms.poc.plugin.api.ServiceDetector;

public class SnmpDetector implements ServiceDetector {

    @Override
    public void init() {

    }

    @Override
    public String getServiceName() {
        return null;
    }

    @Override
    public void setServiceName(String serviceName) {

    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public void setPort(int port) {

    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public void setTimeout(int timeout) {

    }

    @Override
    public String getIpMatch() {
        return null;
    }

    @Override
    public void setIpMatch(String ipMatch) {

    }

    @Override
    public void dispose() {

    }
}
