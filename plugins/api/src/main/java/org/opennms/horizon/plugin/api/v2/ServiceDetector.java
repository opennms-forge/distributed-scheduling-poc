package org.opennms.horizon.plugin.api.v2;

import java.util.concurrent.CompletableFuture;

public interface ServiceDetector {

    CompletableFuture<ServiceDetectorResults> detect(ServiceDetectorRequest request);

}
