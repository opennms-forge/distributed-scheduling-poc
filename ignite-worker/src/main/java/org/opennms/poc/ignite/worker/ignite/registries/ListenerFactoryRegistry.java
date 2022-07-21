package org.opennms.poc.ignite.worker.ignite.registries;

import org.opennms.poc.plugin.api.ListenerFactory;

public interface ListenerFactoryRegistry {

    ListenerFactory getService(String type);
}
