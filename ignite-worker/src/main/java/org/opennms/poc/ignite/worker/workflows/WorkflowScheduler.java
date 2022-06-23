package org.opennms.poc.ignite.worker.workflows;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.cluster.ClusterTopologyException;
import org.apache.ignite.events.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowScheduler {

    @Autowired
    private Ignite ignite;

    private Map<String, Workflow> localWorkflows = new LinkedHashMap<>();
    private Map<String, Timer> timers = new LinkedHashMap<>();

    @PostConstruct
    public void events() {
        watchTheCluster();
        scheduleWorkflows();
    }

    private void watchTheCluster() {
        System.out.println("Watching the cluster");
        ignite.events().localListen(event -> {
            System.out.println("Got event " + event);
            scheduleWorkflows();
            return false;
        }, EventType.EVT_NODE_LEFT, EventType.EVT_NODE_JOINED);
    }

    private void scheduleOrUpdateWorkflow(Workflow workflow) {
        Workflow existingWorkflow = localWorkflows.get(workflow.getUuid());
        if (existingWorkflow == null) {
            System.out.println("Scheduling new workflow: " + workflow);
            localWorkflows.put(workflow.getUuid(), workflow);

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Executing workflow locally: " + workflow);
                    ignite.compute(
                                    ignite.cluster())
                            .broadcastAsync(() -> {
                                System.out.printf("(%s,%s): executed at %d\n", workflow.getType(), workflow.getUuid(), System.currentTimeMillis());
                            });
                }
            }, 0, Integer.parseInt(workflow.getCron()));
            timers.put(workflow.getUuid(), timer);
        } else if (!Objects.equals(workflow, existingWorkflow)) {
            System.out.println("Updating workflow: " + workflow);
        } else {
            System.out.println("No change to workflow: " + workflow);
        }
    }

    private void unscheduleWorkflow(Workflow workflow) {
        if (localWorkflows.remove(workflow.getUuid()) != null) {
            timers.remove(workflow.getUuid()).cancel();
            System.out.println("Removed workflow: " + workflow);
        }
    }

    public synchronized void scheduleWorkflows() {
        // Determine hash
        long topologyVersion = ignite.cluster().topologyVersion();
        List<ClusterNode> clusterNodes = ignite.cluster().topology(topologyVersion).stream()
                .sorted(Comparator.comparing(ClusterNode::id))
                .collect(Collectors.toList());
        int localIndex = IntStream.range(0, clusterNodes.size())
                .filter(i -> clusterNodes.get(i).isLocal())
                .findFirst()
                .orElse(-1);
        int numNodes = clusterNodes.size();

        System.out.printf("Scheduling workflows %d/%d.\n", localIndex, numNodes);
        IgniteCache<String, Workflow> cache = ignite.getOrCreateCache("workflows");
        Set<String> uuidsForHandledWorkflows = new LinkedHashSet<>();
        while(true) {
            try {
                cache.forEach(e -> {
                    BigInteger index = new BigInteger(e.getKey().getBytes(StandardCharsets.UTF_8));
                    int k = index.mod(BigInteger.valueOf(numNodes)).intValue();
                    System.out.printf("Workflow has index:%s mod%d=%s.\n", index, numNodes, k);
                    if (k == localIndex) {
                        // We should be handling this workflow
                        scheduleOrUpdateWorkflow(e.getValue());
                        uuidsForHandledWorkflows.add(e.getKey());
                    }
                });
                break;
            } catch (ClusterTopologyException cte) {
                // pass
            }
        }

        // Remove other workflows that we're not supposed to be running
        Set<String> uuidsToRemove = localWorkflows.keySet().stream()
                .filter(uuid -> !uuidsForHandledWorkflows.contains(uuid))
                .collect(Collectors.toSet());
        uuidsToRemove.forEach(uuid -> unscheduleWorkflow(localWorkflows.get(uuid)));
    }
}
