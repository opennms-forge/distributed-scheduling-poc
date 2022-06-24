package org.opennms.poc.ignite.worker.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;

public class IgnitionFactory {

    public static Ignite create(IgniteConfiguration igniteConfiguration) {
//        return Ignition.start(igniteConfiguration);
        return Ignition.start(new IgniteConfiguration());
    }

}
