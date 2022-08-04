package org.opennms.poc.ignite.worker.ignite;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.PartitionLossPolicy;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.kubernetes.configuration.KubernetesConnectionConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.metric.MetricExporterSpi;
import org.opennms.poc.ignite.worker.ignite.classloader.CompoundClassLoader;
import org.opennms.poc.ignite.worker.workflows.impl.WorkflowLifecycleManagerImpl;

@Data
@AllArgsConstructor
public class WorkerIgniteConfiguration {

    private final MetricExporterSpi metricExporterSpi;
    private final boolean useKubernetes;
    private final String kubernetesServiceName;

    public IgniteConfiguration prepareIgniteConfiguration() {
        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();

        igniteConfiguration.setClientMode(false);
        igniteConfiguration.setMetricsLogFrequency(0);  // DISABLE IGNITE METRICS

        if (useKubernetes) {
            configureClusterNodeDiscoveryKubernetes(igniteConfiguration);
        } else {
            configureClusterNodeDiscovery(igniteConfiguration);
        }

        configureDataStorage(igniteConfiguration);
        configureCache(igniteConfiguration, "workflows");
        configureCache(igniteConfiguration, WorkflowLifecycleManagerImpl.WORKFLOW_SERVICE_CACHE_NAME);

        configureClassLoader(igniteConfiguration);
        igniteConfiguration.setMetricExporterSpi(metricExporterSpi);

        return igniteConfiguration;
    }

    public Ignite startIgnite(IgniteConfiguration igniteConfiguration) {
        return Ignition.start(igniteConfiguration);
    }

//========================================
// Internals
//----------------------------------------

    private void configureClassLoader(IgniteConfiguration igniteConfiguration) {
        // Required for OSGI, otherwise ignite has trouble finding our application classes for (un)marshalling.  Need
        //  a composite class loader to make sure ignite can still find its own internals as well.

        CompoundClassLoader compoundClassLoader =
                new CompoundClassLoader(this,
                        Arrays.asList(this.getClass().getClassLoader(), Ignite.class.getClassLoader()));

        igniteConfiguration.setClassLoader(compoundClassLoader);
    }

    private void configureClusterNodeDiscovery(IgniteConfiguration igniteConfiguration) {
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();

        // Using defaults for now (multicast group 228.1.2.4, port 47400)
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        tcpDiscoverySpi.setIpFinder(ipFinder);

        igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);
    }

    private void configureClusterNodeDiscoveryKubernetes(IgniteConfiguration igniteConfiguration) {
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();

        KubernetesConnectionConfiguration connectionConfiguration = new KubernetesConnectionConfiguration();
        connectionConfiguration.setServiceName(kubernetesServiceName);

        TcpDiscoveryKubernetesIpFinder ipFinder = new TcpDiscoveryKubernetesIpFinder(connectionConfiguration);
        tcpDiscoverySpi.setIpFinder(ipFinder);

        igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);
    }

    private void configureDataStorage(IgniteConfiguration igniteConfiguration) {
        DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration();
        dataStorageConfiguration.getDefaultDataRegionConfiguration().setPersistenceEnabled(false);

        igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);
    }

    private void configureCache(IgniteConfiguration igniteConfiguration, String cacheName) {
        CacheConfiguration<?,?> cacheConfiguration = new CacheConfiguration<>(cacheName);

        cacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
        cacheConfiguration.setBackups(2);
        cacheConfiguration.setRebalanceMode(CacheRebalanceMode.SYNC);
        cacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
        cacheConfiguration.setPartitionLossPolicy(PartitionLossPolicy.READ_ONLY_SAFE);

        igniteConfiguration.setCacheConfiguration(cacheConfiguration);
    }
}
