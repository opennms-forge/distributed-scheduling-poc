package org.opennms.poc.plugin.api;

import java.util.Map;

public interface ServiceMonitorRequest {

    /**
     * The target
     */
    MonitoredService getService();

    /**
     * TECHDEBT: Values may be objects and not just simple types
     * See page-sequence element here for example:
     *  https://github.com/OpenNMS/opennms/blob/opennms-30.0.0-1/opennms-services/src/test/resources/etc/psm-poller-configuration.xml#L25
     */
    Map<String, Object> getParameters();

}

