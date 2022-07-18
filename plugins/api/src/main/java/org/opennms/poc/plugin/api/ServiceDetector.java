package org.opennms.poc.plugin.api;

import java.util.concurrent.CompletableFuture;

public interface ServiceDetector {

    CompletableFuture<ServiceDetectorResults> detect(ServiceDetectorRequest request);

}
