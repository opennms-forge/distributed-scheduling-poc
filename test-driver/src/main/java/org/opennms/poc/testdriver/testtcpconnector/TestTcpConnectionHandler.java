package org.opennms.poc.testdriver.testtcpconnector;

import org.opennms.poc.scheduler.OpennmsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * NOTE: simple implementation using OpennmsScheduler to periodically send samples
 */
public class TestTcpConnectionHandler {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TestTcpConnectionHandler.class);

    private Logger log = DEFAULT_LOGGER;

    private final OpennmsScheduler scheduler;
    private final Socket socket;
    private final String schedule;
    private final int id;

    public TestTcpConnectionHandler(OpennmsScheduler scheduler, Socket socket, int id, String schedule) {
        this.scheduler = scheduler;
        this.schedule = schedule;
        this.socket = socket;
        this.id = id;
    }

    public void start() {
        if (schedule.trim().matches("^\\d+$")) {
            int period = Integer.parseInt(schedule);
            scheduler.schedulePeriodically("task#" + id, period, TimeUnit.MILLISECONDS, this::fireSample);
        } else {
            scheduler.scheduleTaskOnCron("task#" + id, schedule, this::fireSample);
        }
    }

    public void stop() {
        scheduler.cancelTask("task#" + id);
        try {
            socket.close();
        } catch (IOException ioException) {
            log.info("Error closing socket", ioException);
        }
    }

//========================================
// Internal Methods
//----------------------------------------

    private void fireSample() {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write('U');
        } catch (IOException ioException) {
            log.warn("Error writing to connection", ioException);
            stop();
        }
    }
}
