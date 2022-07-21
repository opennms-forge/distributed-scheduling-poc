package org.opennms.poc.ignite.worker.workflows;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;
import org.opennms.poc.ignite.model.workflows.Network;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.model.workflows.WorkflowType;

public class WorkflowGenerator {

    private final Network network;

    public WorkflowGenerator(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    public synchronized List<Workflow> getWorkflows() {
        List<Workflow> workflows = new LinkedList<>();
        getServices().stream().flatMap(s -> this.getWorkflowsForService(s).stream())
                .forEach(workflows::add);
        // Node scan every hour
        for (int nodeIndex = 0; nodeIndex < network.getNumNodes(); nodeIndex++) {
            Workflow nodeScanWorkflow = new Workflow();
            nodeScanWorkflow.setUuid(UUID.randomUUID().toString());
            nodeScanWorkflow.setType(WorkflowType.DETECTOR);
            nodeScanWorkflow.setPluginName("NodeScan");
            nodeScanWorkflow.setCron(Long.toString(TimeUnit.HOURS.toMillis(1)));
            nodeScanWorkflow.setParameters(ImmutableMap.<String,String>builder()
                    .put("host", getIpAddressForNode(nodeIndex))
                    .put("timeout-ms", "500")
                    .put("retries", "1")
                    .put("type", "v2c")
                    .put("community", "n0t-publ1c")
                    .build());
            workflows.add(nodeScanWorkflow);
        }
        return workflows;
    }

    private List<Workflow> getWorkflowsForService(Service service) {
        List<Workflow> workflows = new LinkedList<>();
        // Poll every 30 seconds
        Workflow icmpPollWorkflow = new Workflow();
        icmpPollWorkflow.setUuid(UUID.randomUUID().toString());
        icmpPollWorkflow.setType(WorkflowType.DETECTOR);
        icmpPollWorkflow.setPluginName("IcmpMonitor");
        icmpPollWorkflow.setCron(Long.toString(TimeUnit.SECONDS.toMillis(30)));
        icmpPollWorkflow.setParameters(ImmutableMap.<String,String>builder()
                .put("host", service.getIpAddress())
                .put("timeout-ms", "500")
                .put("retries", "1")
                .put("packet-size-bytes", "500")
                .put("allow-fragmentation", "true")
                .build());
        workflows.add(icmpPollWorkflow);
        // Collect every 1 minute
        Workflow snmpCollectWorkflow = new Workflow();
        snmpCollectWorkflow.setUuid(UUID.randomUUID().toString());
        snmpCollectWorkflow.setType(WorkflowType.DETECTOR);
        snmpCollectWorkflow.setPluginName("SnmpListener");
        snmpCollectWorkflow.setCron(Long.toString(TimeUnit.MINUTES.toMillis(1)));
        snmpCollectWorkflow.setParameters(ImmutableMap.<String,String>builder()
                .put("host", service.getIpAddress())
                .put("timeout-ms", "500")
                .put("retries", "1")
                .put("type", "v2c")
                .put("community", "n0t-publ1c")
                .build());
        workflows.add(snmpCollectWorkflow);
        return workflows;
    }

    /**
     * Cartesian product of nodes, interfaces & services
     *
     * @return list of services computed from network topology
     */
    private List<Service> getServices() {
        List<Service> services = new LinkedList<>();
        for (int nodeIndex = 0; nodeIndex < network.getNumNodes(); nodeIndex++) {
            for (int interfaceIndex = 0; interfaceIndex < network.getNumInterfacesPerNode(); interfaceIndex++) {
                for (int serviceIndex = 0; serviceIndex < network.getNumServicesPerInterface(); serviceIndex++) {
                    services.add(new Service(nodeIndex, interfaceIndex, serviceIndex));
                }
            }
        }
        return services;
    }

    private String getIpAddressForNode(int nodeIndex) {
        return String.format("127.%d.%d.%d", nodeIndex % 256, nodeIndex % 256, (nodeIndex % 254) + 1);
    }

    private static class Service {
        private final int nodeIndex;
        private final int interfaceIndex;
        private final int serviceIndex;

        private Service(int nodeIndex, int interfaceIndex, int serviceIndex) {
            this.nodeIndex = nodeIndex;
            this.interfaceIndex = interfaceIndex;
            this.serviceIndex = serviceIndex;
        }

        public String getIpAddress() {
            return String.format("127.%d.%d.%d", nodeIndex % 256, interfaceIndex % 256, (serviceIndex % 254) + 1);
        }
    }

}
