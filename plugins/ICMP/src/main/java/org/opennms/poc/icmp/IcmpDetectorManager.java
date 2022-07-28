package org.opennms.poc.icmp;

import java.util.function.Consumer;
import org.opennms.poc.plugin.api.ServiceDetector;
import org.opennms.poc.plugin.api.ServiceDetectorManager;
import org.opennms.poc.plugin.api.ServiceDetectorResults;
import org.opennms.poc.plugin.api.annotations.HorizonConfig;

public class IcmpDetectorManager implements ServiceDetectorManager {

    @HorizonConfig(displayName = "sampleConfig")
    public String configValue;

    @Override
    public ServiceDetector create(Consumer<ServiceDetectorResults> resultProcessor) {
        return new IcmpDetector(configValue);
    }
}
