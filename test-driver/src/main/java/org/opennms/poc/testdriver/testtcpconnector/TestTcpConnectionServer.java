package org.opennms.poc.testdriver.testtcpconnector;

import org.opennms.poc.scheduler.OpennmsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TestTcpConnectionServer {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TestTcpConnectionServer.class);

    private Logger log = DEFAULT_LOGGER;

    @Value("${test-tcp-server.port:9980}")
    private int port;

    @Value("${default-schedule:5000}")
    private String defaultSchedule;

    @Autowired
    private OpennmsScheduler opennmsScheduler;

    private ServerSocket serverSocket;
    private Thread acceptorThread;
    private boolean shutdownInd = false;
    private int connectionIdCounter = 0;
    private Map<Integer, TestTcpConnectionHandler> handlers = new ConcurrentHashMap<>();

    @PostConstruct
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        acceptorThread = new Thread(this::acceptConnectLoop);

        acceptorThread.start();
    }

    @PreDestroy
    public void shutdown() {
        shutdownInd = true;
        if (acceptorThread != null) {
            acceptorThread.interrupt();
        }
    }

    public boolean stopConnection(int connectionId) {
        TestTcpConnectionHandler handler = handlers.remove(connectionId);
        if (handler != null) {
            handler.stop();
            return true;
        } else {
            return false;
        }
    }

    public void setDefaultSchedule(String defaultSchedule) {
        this.defaultSchedule = defaultSchedule;
    }

//========================================
// Internals
//----------------------------------------

    private void acceptConnectLoop() {
        while (! shutdownInd) {
            acceptConnection();
        }
    }

    private void acceptConnection() {
        try {
            Socket socket = serverSocket.accept();
            processConnection(socket);
        } catch (Exception exc) {
            log.error("Failed to accept connection", exc);
        }
    }

    private void processConnection(Socket socket) {
        int id = connectionIdCounter++;
        log.info("STARTING connection: id={}", id);

        TestTcpConnectionHandler handler = new TestTcpConnectionHandler(opennmsScheduler, socket, id, defaultSchedule);
        handler.start();

        handlers.put(id, handler);
    }
}
