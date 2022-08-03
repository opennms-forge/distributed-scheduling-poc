package org.opennms.poc.alerting;

import org.opennms.poc.ignite.model.workflows.PluginMetadata;

public interface AlertingService {
    void notifyOfPluginRegistration(PluginMetadata pluginMetadataJson);

}
