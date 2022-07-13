package org.opennms.poc.plugin.api;

import java.util.Map;

public interface ServiceMonitorResponse {
    /**
     *
     * @return status whether service is Unknown/Up/Down/Unresponsive
     */
    Status getStatus();

    /**
     *
     * @return reason behind the current poll status when the service is not Up
     */
    String getReason();

    /**
     * TECHDEBT: Mostly unstructured, but some known properties are used.
     * See https://github.com/OpenNMS/opennms/blob/opennms-30.0.0-1/features/poller/monitors/core/src/main/java/org/opennms/netmgt/poller/monitors/StrafePingMonitor.java#L135
     * .
     * @return map of properties
     */
    Map<String, Number> getProperties();

    /**
     * TECHDEBT: Was originally added to the monitor interface to take advantage of poller's scheduling and
     * configuration mechanism.
     */
    DeviceConfig getDeviceConfig();

    interface DeviceConfig {

        byte[] getContent();

        String getFilename();

    }

    enum Status {
        /**
         * Was unable to determine the status.
         */
        Unknown,

        /**
         * Was in a normal state.
         */
        Up,

        /**
         * Not working normally.
         */
        Down,

        /**
         * Service that is up but is most likely suffering due to excessive load or latency
         * issues and because of that has not responded within the configured timeout period.
         */
        Unresponsive;
    }
}
