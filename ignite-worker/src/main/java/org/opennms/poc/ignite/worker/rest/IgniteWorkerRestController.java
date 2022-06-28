package org.opennms.poc.ignite.worker.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCountDownLatch;
import org.apache.ignite.services.ServiceConfiguration;
import org.apache.ignite.services.ServiceDescriptor;
import org.opennms.poc.ignite.worker.ignite.service.AllRepeatedService;
import org.opennms.poc.ignite.worker.ignite.service.NoopService;
import org.opennms.poc.ignite.worker.ignite.service.WorkflowService;
import org.opennms.poc.ignite.worker.workflows.Network;
import org.opennms.poc.ignite.worker.workflows.Workflow;
import org.opennms.poc.ignite.worker.workflows.WorkflowGenerator;
import org.opennms.poc.ignite.worker.workflows.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Sets;

@RestController
@RequestMapping("/ignite-worker")
public class IgniteWorkerRestController {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(IgniteWorkerRestController.class);

    private Logger log = DEFAULT_LOGGER;

    @Autowired
    private Ignite ignite;

    @Autowired
    private WorkflowRepository workflowRepository;

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
    public String deployNoopService(
            @RequestParam(value = "count", defaultValue = "1") int count,
            @RequestParam(value = "batch", defaultValue = "false") boolean batch) {

        int cur;

        // Create a CountDownLatch to know when all the services have finished starting
        UUID latchUuid = UUID.randomUUID();
        String latchName = latchUuid.toString();
        IgniteCountDownLatch latch = ignite.countDownLatch(latchName, count, true, true);
        long startTimestamp = System.nanoTime();

        // Remember the last service name
        String lastServiceName = "noop-service-" + ( count - 1 );

        // Pre-allocate
        ServiceConfiguration[] serviceConfigs = new ServiceConfiguration[count];
        cur = 0;
        while (cur < count) {
            String serviceName = "noop-service-" + cur;

            serviceConfigs[cur] = new ServiceConfiguration();
            serviceConfigs[cur].setName(serviceName);
            serviceConfigs[cur].setService(new NoopService(lastServiceName, latchName));
            serviceConfigs[cur].setCacheName("noop-services");
            serviceConfigs[cur].setAffinityKey(cur);

            serviceConfigs[cur].setTotalCount(1);

            cur++;
        }

        long preallocTimestamp = System.nanoTime();
        System.out.println(
                "ALLOCATED " + cur + " NO-OP SERVICE INSTANCES IN " + formatElapsedTime(startTimestamp, preallocTimestamp)
        );

        //
        // Now deploy
        //
        if (batch) {
            System.out.println("BATCH DEPLOY");
            ignite.services().deployAllAsync(Arrays.asList(serviceConfigs));
        } else {
            System.out.println("INDIVIDUAL DEPLOY");
            cur = 0;
            while (cur < count ) {
                ignite.services().deployAsync(serviceConfigs[cur]);
                cur++;
            }
        }

        long deployAsyncTimestamp = System.nanoTime();

        System.out.println(
                "INITIATED START OF " + cur + " NO-OP SERVICE INSTANCES IN " + formatElapsedTime(startTimestamp, deployAsyncTimestamp)
        );

        //
        // Wait for startup to complete
        //
        latch.await();

        long finishTimestamp = System.nanoTime();
        String msg = "COMPLETED START OF " + cur + " NO-OP SERVICE INSTANCES IN " + formatElapsedTime(startTimestamp, finishTimestamp);
        System.out.println(
                msg
        );

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

    private ServiceConfiguration toServiceConfiguration(Workflow workflow) {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        serviceConfiguration.setName(workflow.getUuid());
        serviceConfiguration.setService(new WorkflowService(workflow));
        serviceConfiguration.setTotalCount(1);
        return serviceConfiguration;
    }

    @GetMapping(path = "/load-em-up")
    public void loadEmUp(@RequestParam(value = "size", defaultValue = "SMALL") Network.NetworkSize size) {
        WorkflowGenerator workflowGenerator = new WorkflowGenerator(Network.ofSize(size));
        List<Workflow> workflows = workflowGenerator.getWorkflows();

        // Determine which workflows are already scheduled
        Set<String> existingUuids = ignite.services().serviceDescriptors().stream()
                // Only consider workflow services
                .filter(s -> s.serviceClass().equals(WorkflowService.class))
                .map(ServiceDescriptor::name)
                .collect(Collectors.toSet());

        // Schedule workflows that are not already scheduled
        Set<String> uuids = new HashSet<>();
        for (Workflow workflow : workflows) {
            if (!existingUuids.contains(workflow.getUuid())) {
                ignite.services().deployAsync(toServiceConfiguration(workflow));
            }
            // collect the UUIDs as we iterate
            uuids.add(workflow.getUuid());
        }

        // Un-schedule workflows that we're previously scheduled, and are no longer presentc
        for (String uuid : Sets.difference(existingUuids, uuids)) {
            ignite.services().cancelAsync(uuid);
        }
    }

    @GetMapping(path = "/test-thread-startup")
    public void testThreadStartup(@RequestParam(value = "count", defaultValue="10") int count) {
        List<Thread> threads = new LinkedList<>();

        AtomicInteger misc = new AtomicInteger(0);
        long startupTimestamp = System.nanoTime();
        int cur = 0;
        while (cur < count) {
            threads.add(new Thread(misc::incrementAndGet));
            cur++;
        }

        threads.forEach(Thread::start);

        threads.forEach((thread) -> {
            try {
                thread.join();
            } catch (InterruptedException intExc) {
                intExc.printStackTrace();
            }
        });

        long finishTimestamp = System.nanoTime();

        System.out.println("STARTUP " + count + " THREADS: " + formatElapsedTime(startupTimestamp, finishTimestamp) + "; check=" + misc.get());
    }

//========================================
// Internals
//----------------------------------------

    private String formatElapsedTime(long firstTimestamp, long secondTimestamp) {
        long diffNano = secondTimestamp - firstTimestamp;
        long diffSec = diffNano / 1000000000L;
        long diffRemainingMilli = ( diffNano / 1000000L ) % 1000L;

        return diffSec + "s " + diffRemainingMilli + "ms";
    }
}
