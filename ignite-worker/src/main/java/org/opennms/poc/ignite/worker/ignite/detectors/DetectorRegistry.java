package org.opennms.poc.ignite.worker.ignite.detectors;

public interface DetectorRegistry {

    Detector getService(String type);
}
