package org.opennms.poc.testtcpconnector;

import org.opennms.poc.plugin.api.ParameterMap;
import org.opennms.poc.plugin.api.ServiceConnector;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.api.ServiceMonitorResponseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * NOTE: uses 1 thread per instance. Does NOT scale well.
 */
public class TestTcpConnector implements ServiceConnector {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TestTcpConnector.class);

    private Logger log = DEFAULT_LOGGER;


    private Consumer<ServiceMonitorResponse> resultProcessor;
    private Map<String, Object> parameters;
    private Runnable disconnectHandler;
    private Socket clientSocket;
    private Thread receiverThread;
    private boolean shutdownInd = false;

    private long counter = 0;

    public TestTcpConnector(Consumer<ServiceMonitorResponse> resultProcessor, Map<String, Object> parameters, Runnable disconnectHandler) {
        this.resultProcessor = resultProcessor;
        this.parameters = parameters;
        this.disconnectHandler = disconnectHandler;
        receiverThread = new Thread(this::processIncomingSamples);
        receiverThread.start();
    }

    @Override
    public void connect() throws Exception {
        shutdownInd = false;
        createSocketConnection();
    }

    @Override
    public void disconnect() {
        shutdownInd = true;
        receiverThread.interrupt();
    }

//========================================
// Internals
//----------------------------------------

    private void createSocketConnection() throws Exception {
        String hostname = ParameterMap.getKeyedString(parameters, "host", "localhost");
        int port = ParameterMap.getKeyedInteger(parameters, "port", 9980);

        log.info("CONNECTING TO {}:{}", hostname, port);

        clientSocket = new Socket(hostname, port);
    }

    private void processIncomingSamples() {
        while (! shutdownInd) {
            try {
                if (clientSocket != null) {
                    log.debug("Waiting on a sample");

                    int value = clientSocket.getInputStream().read();
                    if (value != -1) {
                        processOneSample(value);
                    } else {
                        log.info("EOF on connection");
                        processDisconnect();
                    }
                } else {
                    log.debug("Busy-waiting for connection");

                    // Wait 0.1 sec and try again
                    Thread.sleep(100);
                }
            } catch (IOException ioExc) {
                log.warn("Error on connection", ioExc);
                processDisconnect();
            } catch (InterruptedException interruptedException) {
                log.debug("Interrupted waiting for socket connection", interruptedException);
            }
        }
    }

    private void processDisconnect() {
        try {
            clientSocket.close();
            clientSocket = null;
        } catch (IOException ioExc) {
            log.debug("Exception on making sure socket is closed at disconnect", ioExc);
        }

        disconnectHandler.run();
    }

    private void processOneSample(int value) {
        ServiceMonitorResponse response;

        Map<String, Number> properties = new HashMap<>();
        properties.put("sample", value);

        switch (value) {
            case 'U':
            case 'u':
                response =
                        ServiceMonitorResponseImpl.builder()
                                .status(ServiceMonitorResponse.Status.Up)
                                .reason("Test TCP Connector Sample")
                                .properties(properties)
                                .build()
                        ;
                break;

            case 'D':
            case 'd':
                response =
                        ServiceMonitorResponseImpl.builder()
                                .status(ServiceMonitorResponse.Status.Down)
                                .reason("Test TCP Connector Sample")
                                .build()
                        ;
                break;

            default:
                response =
                        ServiceMonitorResponseImpl.builder()
                                .status(ServiceMonitorResponse.Status.Unknown)
                                .reason("Test TCP Connector Sample; sample-value=" + value)
                                .build()
                ;
        }


        resultProcessor.accept(response);
    }
}
