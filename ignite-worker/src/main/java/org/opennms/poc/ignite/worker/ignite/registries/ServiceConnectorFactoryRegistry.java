package org.opennms.poc.ignite.worker.ignite.registries;

import org.opennms.poc.plugin.api.ServiceConnectorFactory;

public interface ServiceConnectorFactoryRegistry {

    ServiceConnectorFactory getService(String type);
}
