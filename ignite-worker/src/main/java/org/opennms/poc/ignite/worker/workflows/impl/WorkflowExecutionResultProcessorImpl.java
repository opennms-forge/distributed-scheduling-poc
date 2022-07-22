package org.opennms.poc.ignite.worker.workflows.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Setter;
import org.opennms.poc.ignite.model.workflows.Result;
import org.opennms.poc.ignite.model.workflows.Results;
import org.opennms.poc.ignite.worker.queue.impl.AsyncProcessingQueueImpl;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutionResultProcessor;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class WorkflowExecutionResultProcessorImpl implements WorkflowExecutionResultProcessor {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowExecutionResultProcessorImpl.class);
    private final Consumer<ServiceMonitorResponse> consumer;

    private Logger log = DEFAULT_LOGGER;

    private AsyncProcessingQueueImpl<ServiceMonitorResponse> queue;

    @Setter
    private ThreadPoolExecutor executor;

    @Setter
    private int maxQueueSize = AsyncProcessingQueueImpl.DEFAULT_MAX_QUEUE_SIZE;

    public WorkflowExecutionResultProcessorImpl(Consumer<Results> consumer) {
        this.consumer = new Consumer<ServiceMonitorResponse>() {
            @Override
            public void accept(ServiceMonitorResponse serviceMonitorResponse) {
                Map<String, Number> responseProperties = serviceMonitorResponse.getProperties();
                Results results = new Results();
                Result result = new Result();
                result.setParameters(new LinkedHashMap<>(responseProperties));
                results.getResults().add(result);
                consumer.accept(results);
            }
        };
    }

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
    public void queueSendResult(ServiceMonitorResponse result) {
        queue.asyncSend(result);
    }

//========================================
// Downstream
//----------------------------------------

    private void stubConsumer(ServiceMonitorResponse result) {
        try {
            // TBD: REMOVE the json mapping - feed response back to Core
            log.info("O-POLL STATUS: " + new ObjectMapper().writeValueAsString(result));
            consumer.accept(result);
        } catch (JsonProcessingException jpExc) {
            log.warn("error processing workflow result", jpExc);
        }
    }
}
