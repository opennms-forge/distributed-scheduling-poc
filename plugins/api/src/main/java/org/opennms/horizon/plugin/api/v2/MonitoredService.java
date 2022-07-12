package org.opennms.horizon.plugin.api.v2;

import java.net.InetAddress;

public interface MonitoredService {

    /**
     * Returns the svcName associated with this monitored service.
     *
     * @return the svcName
     */
    String getSvcName();

    /**
     * Returns the ipAddr string associated with this monitored service.
     *
     * @return the ipAddr string
     */
    String getIpAddr();

    /**
     * Returns the nodeId of the node that this service is associated with.
     *
     * @return the nodeid
     */
    int getNodeId();

    /**
     * Returns the label of the node that this service is associated with.
     *
     * @return the nodelabel
     */
    String getNodeLabel();

    /**
     * Returns the name of the location of the node that this service is associated with.
     *
     * @return the nodelocation
     */
    String getNodeLocation();

    /**
     * Returns the {@link InetAddress} associated with the service
     *
     * @return the {@link InetAddress}
     */
    InetAddress getAddress();

}
