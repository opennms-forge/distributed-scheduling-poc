package org.opennms.poc.fakeconnector;

import org.opennms.poc.plugin.api.ServiceConnector;
import org.opennms.poc.plugin.api.ServiceConnectorFactory;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;

import java.util.Map;
import java.util.function.Consumer;

public class FakeConnectorFactory implements ServiceConnectorFactory {
    @Override
    public ServiceConnector create(Consumer<ServiceMonitorResponse> resultProcessor, Map<String, Object> parameters, Runnable disconnectHandler) {
        return new FakeConnector(resultProcessor, parameters, disconnectHandler);
    }
}
