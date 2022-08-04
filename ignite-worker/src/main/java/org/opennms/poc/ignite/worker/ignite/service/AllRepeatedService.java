package org.opennms.poc.ignite.worker.ignite.service;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.Service;

@Slf4j
public class AllRepeatedService implements Service {
    private static final long serialVersionUID = 0L;

    private final UUID myNodeId;
    @IgniteInstanceResource
    private Ignite ignite;

    public AllRepeatedService(UUID myNodeId) {
        this.myNodeId = myNodeId;
    }

    @Override
    public void init() throws Exception {
        System.out.println("INIT hi-all-repeated service triggered by " + myNodeId + ", running on " + ignite.cluster().localNode().id());
    }

    @Override
    public void execute() throws Exception {
        System.out.println("EXEC hi-all-repeated service triggered by " + myNodeId + ", running on " + ignite.cluster().localNode().id());
    }

    @Override
    public void cancel() {
        System.out.println("CANCEL hi-all-repeated service triggered by " + myNodeId + ", running on " + ignite.cluster().localNode().id());
    }
}
