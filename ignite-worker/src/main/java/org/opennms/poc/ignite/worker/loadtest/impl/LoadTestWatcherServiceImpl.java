package org.opennms.poc.ignite.worker.loadtest.impl;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.LoggerResource;
import org.opennms.poc.ignite.worker.loadtest.LoadTestWatcherService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTestWatcherServiceImpl implements LoadTestWatcherService {

    /**
     * Limit of a ping's age before reporting it's old.
     */
    public static final long REPORT_PING_AGE_LIMIT = 30_000;

    public static final long REPORT_PERIOD = 5_000;

    @IgniteInstanceResource
    private Ignite ignite;

    @LoggerResource
    private IgniteLogger logger;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private Map<String, Long> pingTimestamps;
    private AtomicLong pingsByMessage = new AtomicLong(0);
    private AtomicLong pingsByServiceCall = new AtomicLong(0);

    private final Object lock = new Object();

    @Override
    public void init() throws Exception {
        UUID myNodeId = ignite.cluster().localNode().id();
        logger.info("STARTING " + SERVICE_NAME + " SERVICE ON NODE " + myNodeId);

        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        pingTimestamps = new HashMap<>();

        //
        // REGISTER for "PING" messages
        //
        ignite.message().remoteListen("PING", this::processPingMessage);
    }

    @Override
    public void execute() throws Exception {
        scheduledThreadPoolExecutor.scheduleAtFixedRate(this::runPeriodicChecks, REPORT_PERIOD, REPORT_PERIOD, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancel() {
        if (scheduledThreadPoolExecutor != null) {
            scheduledThreadPoolExecutor.shutdown();
        }
    }

    @Override
    public void servicePing(String serviceName) {
        pingsByServiceCall.incrementAndGet();
        processPingInternal(serviceName);
    }

    @Override
    public void servicesAdding(List<String> serviceNames) {
        synchronized (lock) {
            serviceNames.forEach((name) -> pingTimestamps.put(name, null));
        }
    }

    @Override
    public Map<String, Long> getPingTimestamps() {
        Map<String, Long> result = new TreeMap<>();
        synchronized (lock) {
            result.putAll(pingTimestamps);
        }

        return result;
    }

//========================================
// Internals
//----------------------------------------

    private boolean processPingMessage(UUID node, Object serviceNameObj) {
        pingsByMessage.incrementAndGet();

        if (serviceNameObj instanceof String) {
            processPingInternal((String) serviceNameObj);
        }

        return true;
    }

    private void processPingInternal(String serviceName) {
        long now = System.nanoTime();

        synchronized (lock) {
            pingTimestamps.put(serviceName, now);
        }
    }

    private void runPeriodicChecks() {
        Map<String, Long> pingTimestampSnapshot;

        synchronized (lock) {
            pingTimestampSnapshot = new HashMap<>();
            pingTimestampSnapshot.putAll(pingTimestamps);
        }

        long now = System.nanoTime();
        long complaintCount = 0;
        long healthyCount = 0;

        for ( Map.Entry<String, Long> entry : pingTimestampSnapshot.entrySet() ) {
            if (entry.getValue() == null) {
                complain(complaintCount, "No ping received for " + entry.getKey());
                complaintCount++;
            } else if ((now - entry.getValue()) > ( REPORT_PING_AGE_LIMIT * 1000000L )) {
                complain(complaintCount, "Service ping is old " + entry.getKey() + "; " + formatElapsedTime(entry.getValue(), now));
                complaintCount++;
            } else {
                healthyCount++;
            }
        }

        logger.info(
                "Finished check; complaint-count=" + complaintCount +
                "; healthy-count=" + healthyCount +
                "; svc-call-ping-count=" + pingsByServiceCall.get() +
                "; ping-by-msg=" + pingsByMessage.get()
        );
    }

    private void complain(long complaintCount, String msg) {
        if (complaintCount < 5) {
            logger.warning(msg);
        }
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