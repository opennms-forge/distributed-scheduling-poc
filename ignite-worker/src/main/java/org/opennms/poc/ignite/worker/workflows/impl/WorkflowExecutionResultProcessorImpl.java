package org.opennms.poc.ignite.worker.workflows.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.opennms.poc.ignite.worker.queue.impl.AsyncProcessingQueueImpl;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutionResultProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class WorkflowExecutionResultProcessorImpl implements WorkflowExecutionResultProcessor {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowExecutionResultProcessorImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private AsyncProcessingQueueImpl<Object> queue;

    @Setter
    private ThreadPoolExecutor executor;

    @Setter
    private int maxQueueSize = AsyncProcessingQueueImpl.DEFAULT_MAX_QUEUE_SIZE;

//========================================
// Lifecycle
//----------------------------------------

    public void init() {
        queue = new AsyncProcessingQueueImpl<>();
        queue.setExecutor(executor);
        queue.setConsumer(this::stubConsumer);
        queue.setMaxQueueSize(maxQueueSize);
        queue.init();
    }


//========================================
// API
//----------------------------------------

    @Override
    public void queueSendResult(Object result) {
        queue.asyncSend(result);
    }

//========================================
// Downstream
//----------------------------------------

    private void stubConsumer(Object result) {
        try {
            // TBD: REMOVE the json mapping - feed response back to Core
            log.info("O-POLL STATUS: " + new ObjectMapper().writeValueAsString(result));
        } catch (JsonProcessingException jpExc) {
            log.warn("error processing workflow result", jpExc);
        }
    }
}
