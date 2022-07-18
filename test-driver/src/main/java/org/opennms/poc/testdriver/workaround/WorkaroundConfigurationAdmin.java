package org.opennms.poc.testdriver.workaround;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Properties;

/**
 * WORKAROUND: adapter for the OSGI ConfigurationAdmin, which is tightly coupled into parts of Horizon Stream's GRPC
 * code, which returns a single, fixed configuration (i.e. set of properties).
 *
 * NOTE: most operations throw UnsupportedOperationException.
 */
public class WorkaroundConfigurationAdmin implements ConfigurationAdmin {

    private final WorkaroundConfiguration soleConfiguration;

    public WorkaroundConfigurationAdmin(Properties properties) {
        this.soleConfiguration = new WorkaroundConfiguration(properties);
    }

    @Override
    public Configuration createFactoryConfiguration(String factoryPid) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Configuration createFactoryConfiguration(String factoryPid, String location) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Configuration getConfiguration(String pid, String location) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Configuration getConfiguration(String pid) throws IOException {
        return soleConfiguration;
    }

    @Override
    public Configuration getFactoryConfiguration(String factoryPid, String name, String location) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Configuration getFactoryConfiguration(String factoryPid, String name) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Configuration[] listConfigurations(String filter) throws IOException {
        return new Configuration[0];
    }
}
