package org.opennms.poc.testdriver.workaround;

import org.opennms.core.grpc.common.GrpcIpcServerBuilder;

import java.util.Properties;

/**
 * WORKAROUND: version of the GrpcIpcServerBuilder which uses java.util.Properties for configuration rather than the
 *  OSGI ConfigurationManagement object.
 */
public class WorkaroundGrpcIpcServerBuilder extends GrpcIpcServerBuilder {

    public WorkaroundGrpcIpcServerBuilder(Properties properties, int port, String delay) {
        super(new WorkaroundConfigurationAdmin(properties), port, delay);
    }
}
