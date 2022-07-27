package org.opennms.poc.testtcpconnector;

import org.opennms.poc.plugin.api.ServiceConnector;
import org.opennms.poc.plugin.api.ServiceConnectorFactory;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;

import java.util.Map;
import java.util.function.Consumer;

public class TestTcpConnectorFactory implements ServiceConnectorFactory {
    @Override
    public ServiceConnector create(Consumer<ServiceMonitorResponse> resultProcessor, Map<String, Object> parameters, Runnable disconnectHandler) {
        return new TestTcpConnector(resultProcessor, parameters, disconnectHandler);
    }
}
