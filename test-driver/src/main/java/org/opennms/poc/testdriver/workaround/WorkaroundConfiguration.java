package org.opennms.poc.testdriver.workaround;

import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

/**
 * WORKAROUND: Implementation of the OSGI Configuration interface which simply uses values from a java.util.Properties
 *  source.  Needed due to parts of Horizon Stream that are tightly coupled to the OSGI classes.
 *
 *  NOTE: most methods throw UnsupportedOperationException.
 */
public class WorkaroundConfiguration implements Configuration {

    private final Properties source;

    public WorkaroundConfiguration(Properties source) {
        this.source = source;
    }

    @Override
    public String getPid() {
        return "workaround-config";
    }

    @Override
    public Dictionary<String, Object> getProperties() {
        Hashtable<String, Object> result = new Hashtable<>();
        for (String propertyName : source.stringPropertyNames()) {
            result.put(propertyName, source.get(propertyName));
        }

        return result;
    }

    @Override
    public Dictionary<String, Object> getProcessedProperties(ServiceReference<?> reference) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void update(Dictionary<String, ?> properties) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void delete() throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public String getFactoryPid() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void update() throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean updateIfDifferent(Dictionary<String, ?> properties) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setBundleLocation(String location) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public String getBundleLocation() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public long getChangeCount() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void addAttributes(ConfigurationAttribute... attrs) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Set<ConfigurationAttribute> getAttributes() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void removeAttributes(ConfigurationAttribute... attrs) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }
}
