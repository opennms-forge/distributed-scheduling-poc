package org.opennms.poc.ignite.worker.rest;

import org.apache.ignite.Ignite;
import org.apache.ignite.services.ServiceConfiguration;
import org.opennms.poc.ignite.worker.ignite.service.AllRepeatedService;
import org.opennms.poc.ignite.worker.ignite.service.NoopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/ignite-worker")
public class IgniteWorkerRestController {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(IgniteWorkerRestController.class);

    private Logger log = DEFAULT_LOGGER;

    @Autowired
    private Ignite ignite;

    @GetMapping(path = "/hi-youngest")
    public void hiOnYoungest() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ignite.compute(
            ignite.cluster().forYoungest())
                .runAsync(() -> {
                    System.out.println("YOUNGEST: hello initiated by node " + myNodeId);
                });
    }

    @GetMapping(path = "/hi-oldest")
    public void hiOnOldest() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ignite.compute(
                ignite.cluster().forOldest())
                .runAsync(
                        () -> {
                            System.out.println("OLDEST: hello initiated by node " + myNodeId);
                });
    }

    @GetMapping(path = "/hi-all")
    public void hiAll() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ignite.compute(
                ignite.cluster())
                .broadcastAsync(() -> {
                    System.out.println("ALL: hello initiated by node " + myNodeId);
                });
    }

    @GetMapping(path = "/hi-all-repeated-service")
    public void deployHiAllRepeatedService() {
        UUID myNodeId = ignite.cluster().localNode().id();

        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        serviceConfiguration.setName("Hi All Repeated");
        serviceConfiguration.setService(new AllRepeatedService(myNodeId));

        serviceConfiguration.setTotalCount(1);
        ignite.services().deploy(serviceConfiguration);
    }

    @DeleteMapping(path = "/hi-all-repeated-service")
    public void removeHiAllRepeatedService() {
        ignite.services().cancel("Hi All Repeated");
    }

    @GetMapping(path = "/noop-service")
    public String deployNoopService(@RequestParam(value = "count", defaultValue = "1") int count) {
        int cur;

        String lastServiceName = "noop-service-" + ( count - 1 );

        cur = 0;
        while (cur < count ) {
            String serviceName = "noop-service-" + cur;

            ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
            serviceConfiguration.setName(serviceName);
            serviceConfiguration.setService(new NoopService(lastServiceName));

            serviceConfiguration.setTotalCount(1);
            ignite.services().deployAsync(serviceConfiguration);

            cur++;
        }

        String msg = "STARTED " + cur + " NO-OP SERVICE INSTANCES";
        System.out.println(msg);

        return msg;
    }

    @DeleteMapping(path = "/noop-service")
    public String undeployNoopService(@RequestParam(value = "count", defaultValue = "1") int count) {
        int cur;

        cur = 0;
        while (cur < count ) {
            ignite.services().cancelAsync("noop-service-" + cur);
            cur++;
        }

        String msg = "STOPPING " + cur + " NO-OP SERVICE INSTANCES";
        System.out.println(msg);

        return msg;
    }
}
