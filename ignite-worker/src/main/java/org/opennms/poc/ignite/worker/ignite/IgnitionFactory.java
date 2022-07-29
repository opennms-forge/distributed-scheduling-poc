package org.opennms.poc.ignite.worker.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

public class IgnitionFactory {
    public static Ignite create(WorkerIgniteConfiguration workerIgniteConfiguration) {
        return Ignition.start(workerIgniteConfiguration.prepareIgniteConfiguration());
    }

}
