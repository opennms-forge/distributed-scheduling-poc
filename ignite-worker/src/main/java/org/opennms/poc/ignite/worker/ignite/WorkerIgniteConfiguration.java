package org.opennms.poc.ignite.worker.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.PartitionLossPolicy;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerIgniteConfiguration {

    @Bean
    public IgniteConfiguration prepareIgniteConfiguration() {
        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();

        igniteConfiguration.setClientMode(false);

        this.configureClusterNodeDiscovery(igniteConfiguration);
        this.configureDataStorage(igniteConfiguration);
        this.configureCache(igniteConfiguration);

        return igniteConfiguration;
    }

    @Bean
    public Ignite startIgnite(@Autowired IgniteConfiguration igniteConfiguration) {
        return Ignition.start(igniteConfiguration);
    }

//========================================
// Internals
//----------------------------------------

    private void configureClusterNodeDiscovery(IgniteConfiguration igniteConfiguration) {
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();

        // Using defaults for now (multicast group 228.1.2.4, port 47400)
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        tcpDiscoverySpi.setIpFinder(ipFinder);

        igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);
    }

    private void configureDataStorage(IgniteConfiguration igniteConfiguration) {
        DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration();
        dataStorageConfiguration.getDefaultDataRegionConfiguration().setPersistenceEnabled(false);

        igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);
    }

    private void configureCache(IgniteConfiguration igniteConfiguration) {
        CacheConfiguration<?,?> cacheConfiguration = new CacheConfiguration<>("workflows");

        cacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
        cacheConfiguration.setBackups(2);
        cacheConfiguration.setRebalanceMode(CacheRebalanceMode.SYNC);
        cacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
        cacheConfiguration.setPartitionLossPolicy(PartitionLossPolicy.READ_ONLY_SAFE);

        igniteConfiguration.setCacheConfiguration(cacheConfiguration);
    }
}
