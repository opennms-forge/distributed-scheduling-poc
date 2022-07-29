package org.opennms.poc.ignite.worker.ignite.registries;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutorLocalServiceFactory;
import org.opennms.poc.plugin.api.ServiceDetectorManager;
import org.opennms.poc.plugin.api.ServiceMonitorManager;
import org.opennms.poc.scheduler.OpennmsScheduler;
import org.osgi.framework.BundleContext;

@Slf4j
public class OsgiServiceHolder {
    private static DetectorRegistry detectorRegistry;
    private static MonitorRegistry monitorRegistry;
    private static ListenerFactoryRegistry listenerFactoryRegistry;
    private static ServiceConnectorFactoryRegistryImpl serviceConnectorFactoryRegistry;
    private static OpennmsScheduler opennmsScheduler;
    private static WorkflowExecutorLocalServiceFactory workflowExecutorLocalServiceFactory;

    public OsgiServiceHolder(
            BundleContext bundleContext,
            OpennmsScheduler opennmsScheduler,
            WorkflowExecutorLocalServiceFactory workflowExecutorLocalServiceFactory,
            ListenerFactoryRegistry listenerFactoryRegistry,
            ServiceConnectorFactoryRegistryImpl serviceConnectorFactoryRegistry,
            ProducerTemplate producerTemplate) {

        log.info("Creating an instance of the StaticDetectorRegistry for initialization. Don't do this twice!");
        detectorRegistry = new DetectorRegistryImpl(bundleContext, producerTemplate);
        monitorRegistry = new MonitorRegistryImpl(bundleContext, producerTemplate);

        OsgiServiceHolder.opennmsScheduler = opennmsScheduler;
        OsgiServiceHolder.workflowExecutorLocalServiceFactory = workflowExecutorLocalServiceFactory;
        OsgiServiceHolder.listenerFactoryRegistry = listenerFactoryRegistry;
        OsgiServiceHolder.serviceConnectorFactoryRegistry = serviceConnectorFactoryRegistry;
    }

    public static Optional<ServiceDetectorManager> getDetectorManager(String name) {
         return Optional.ofNullable(detectorRegistry.getService(name));
    }

    public static int getRegisteredDetectorCount() {
        return detectorRegistry.getServiceCount();
    }

    public static Optional<ServiceMonitorManager> getMonitorManager(String name) {
        return Optional.ofNullable(monitorRegistry.getService(name));
    }

    public static int getRegisteredMonitorCount() {
        return monitorRegistry.getServiceCount();
    }

    public static OpennmsScheduler getOpennmsScheduler() {
        return opennmsScheduler;
    }

    public static WorkflowExecutorLocalServiceFactory getWorkflowExecutorLocalServiceFactory() {
        return workflowExecutorLocalServiceFactory;
    }

    public static ListenerFactoryRegistry getListenerFactoryRegistry() {
        return listenerFactoryRegistry;
    }

    public static ServiceConnectorFactoryRegistryImpl getServiceConnectorFactoryRegistry() {
        return serviceConnectorFactoryRegistry;
    }
}
