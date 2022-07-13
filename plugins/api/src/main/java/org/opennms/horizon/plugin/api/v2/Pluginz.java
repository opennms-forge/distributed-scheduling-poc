package org.opennms.horizon.plugin.api.v2;

import org.opennms.horizon.plugin.api.v2.telemetry.Connector;
import org.opennms.horizon.plugin.api.v2.telemetry.Listener;

/**
 * Used to document the extension points.
 */
public interface Pluginz {

    ServiceDetector aDetector();

    ServiceMonitor aMonitor();

    Connector aConnector();

    Listener aListener();

}
