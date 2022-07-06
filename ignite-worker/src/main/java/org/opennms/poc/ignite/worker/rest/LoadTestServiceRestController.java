package org.opennms.poc.ignite.worker.rest;

import org.apache.ignite.Ignite;
import org.opennms.poc.ignite.worker.loadtest.LoadTestServiceClusterStarter;
import org.opennms.poc.ignite.worker.loadtest.LoadTestWatcherService;
import org.opennms.poc.ignite.worker.loadtest.PingMethod;
import org.opennms.poc.ignite.worker.loadtest.impl.LoadTestServiceClusterBatchStarter;
import org.opennms.poc.ignite.worker.loadtest.impl.LoadTestServiceClusterIndividualStarter;
import org.opennms.poc.ignite.worker.loadtest.impl.LoadTestServiceStarterJobImpl;
import org.opennms.poc.ignite.worker.loadtest.impl.LoadTestWatcherServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/load-test-service")
public class LoadTestServiceRestController {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(LoadTestServiceRestController.class);

    private Logger log = DEFAULT_LOGGER;

    @Autowired
    private Ignite ignite;

    @GetMapping(path = "/init")
    public void initLoadTestService() {
        LoadTestWatcherServiceImpl service = new LoadTestWatcherServiceImpl();

        ignite.services().deployClusterSingleton(LoadTestWatcherService.SERVICE_NAME, service);
    }

    @GetMapping(path = "spin-up/{prefix}")
    public void spinUpServices(
            @PathVariable("prefix") String prefix,
            @RequestParam(value = "count", defaultValue = "1") int count,
            @RequestParam(value = "batch", defaultValue = "false") boolean batchInd,
            @RequestParam(value = "ping-method", defaultValue = "SERVICE_METHODCALL") PingMethod pingMethod,
            @RequestParam(value = "rethrow-task-exception", defaultValue = "true") boolean rethrowTaskException
            ) {
        LoadTestServiceClusterStarter starter;
        if (batchInd) {
            starter = new LoadTestServiceClusterBatchStarter();
        } else {
            starter = new LoadTestServiceClusterIndividualStarter();
        }

        LoadTestServiceStarterJobImpl starterJob =
                new LoadTestServiceStarterJobImpl(prefix, count, starter, pingMethod, rethrowTaskException);

        starterJob.setIgnite(ignite);
        starterJob.setLogger(ignite.log());

        //
        // Just run locally, without going through ignite at all.
        //
        starterJob.run();
    }

    @GetMapping(path = "concurrent-spin-up/{prefix}")
    public void concurrentSpinUpServices(
            @PathVariable("prefix") String prefix,
            @RequestParam(value = "count", defaultValue = "1") int count,
            @RequestParam(value = "batch", defaultValue = "false") boolean batchInd,
            @RequestParam(value = "ping-method", defaultValue = "SERVICE_METHODCALL") PingMethod pingMethod,
            @RequestParam(value = "rethrow-task-exception", defaultValue = "true") boolean rethrowTaskException
    ) {
        LoadTestServiceClusterStarter starter;
        if (batchInd) {
            starter = new LoadTestServiceClusterBatchStarter();
        } else {
            starter = new LoadTestServiceClusterIndividualStarter();
        }
        LoadTestServiceStarterJobImpl starterJob =
                new LoadTestServiceStarterJobImpl(prefix, count, starter, pingMethod, rethrowTaskException);

        ignite.compute().broadcastAsync(starterJob);
    }

    @GetMapping(path = "ping-timestamps", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> readPingTimestamps() {
        return ignite
                .services()
                .serviceProxy(LoadTestWatcherService.SERVICE_NAME, LoadTestWatcherService.class, false)
                .getPingTimestamps();
    }
}
