package org.opennms.poc.ignite.worker.workflows;

public class Network {

    public static final Network SMALL = Network.builder()
            .withNumNodes(200)
            .withNumInterfacesPerNode(2)
            .withNumServicesPerInterface(5)
            .build();

    public static final Network MEDIUM = Network.builder()
            .withNumNodes(2000)
            .withNumInterfacesPerNode(5)
            .withNumServicesPerInterface(3)
            .build();

    public static final Network LARGE = Network.builder()
            .withNumNodes(20000)
            .withNumInterfacesPerNode(10)
            .withNumServicesPerInterface(3)
            .build();

    public static final Network EXTRA_LARGE = Network.builder()
            .withNumNodes(100000)
                .withNumInterfacesPerNode(10)
                .withNumServicesPerInterface(3)
                .build();

    public static final Network ENORMOUS = Network.builder()
            .withNumNodes(500000)
            .withNumInterfacesPerNode(5)
            .withNumServicesPerInterface(3)
            .build();

    public enum NetworkSize {
        SMALL,
        MEDIUM,
        LARGE,
        EXTRA_LARGE,
        ENORMOUS;
    }

    public static Network ofSize(NetworkSize size) {
        switch (size) {
            case SMALL:
                return SMALL;
            case MEDIUM:
                return MEDIUM;
            case LARGE:
                return LARGE;
            case EXTRA_LARGE:
                return EXTRA_LARGE;
            case ENORMOUS:
                return ENORMOUS;
        }
        throw new RuntimeException("Unknown size: " + size);
    }

    private final long numNodes;
    private final long numInterfacesPerNode;
    private final long numServicesPerInterface;

    public Network(long numNodes, long numInterfacesPerNode, long numServicesPerInterface) {
        this.numNodes = numNodes;
        this.numInterfacesPerNode = numInterfacesPerNode;
        this.numServicesPerInterface = numServicesPerInterface;
    }

    public static class NetworkBuilder {
        private long numNodes = 10;
        private long numInterfacesPerNode = 2;
        private long numServicesPerInterface = 3;

        public NetworkBuilder withNumNodes(long numNodes) {
            this.numNodes = numNodes;
            return this;
        }

        public NetworkBuilder withNumInterfacesPerNode(long numInterfacesPerNode) {
            this.numInterfacesPerNode = numInterfacesPerNode;
            return this;
        }

        public NetworkBuilder withNumServicesPerInterface(long numServicesPerInterface) {
            this.numServicesPerInterface = numServicesPerInterface;
            return this;
        }

        public Network build() {
            return new Network(numNodes, numInterfacesPerNode, numServicesPerInterface);
        }
    }

    public static NetworkBuilder builder() {
        return new NetworkBuilder();
    }

    public long getNumNodes() {
        return numNodes;
    }

    public long getNumInterfacesPerNode() {
        return numInterfacesPerNode;
    }

    public long getNumServicesPerInterface() {
        return numServicesPerInterface;
    }

    public long getNumServices() {
        return numNodes * numServicesPerInterface * numInterfacesPerNode;
    }

    public long getNumInterfaces() {
        return numNodes * numServicesPerInterface;
    }
}
