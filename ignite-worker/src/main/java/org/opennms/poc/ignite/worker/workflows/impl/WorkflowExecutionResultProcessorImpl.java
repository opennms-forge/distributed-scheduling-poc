package org.opennms.poc.ignite.worker.workflows.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import lombok.Setter;
import org.opennms.poc.ignite.model.workflows.Result;
import org.opennms.poc.ignite.model.workflows.Results;
import org.opennms.poc.ignite.worker.queue.impl.AsyncProcessingQueueImpl;
import org.opennms.poc.ignite.worker.workflows.WorkflowExecutionResultProcessor;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowExecutionResultProcessorImpl implements WorkflowExecutionResultProcessor {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(WorkflowExecutionResultProcessorImpl.class);
    private final Consumer<Entry> consumer;

    private Logger log = DEFAULT_LOGGER;

    private AsyncProcessingQueueImpl<Entry> queue;

    @Setter
    private ThreadPoolExecutor executor;

    @Setter
    private int maxQueueSize = AsyncProcessingQueueImpl.DEFAULT_MAX_QUEUE_SIZE;

    public WorkflowExecutionResultProcessorImpl(Consumer<Results> consumer) {
        this.consumer = new Consumer<Entry>() {
            @Override
            public void accept(Entry entry) {
                Map<String, Number> responseProperties = entry.result.getProperties();
                Results results = new Results();
                Result result = new Result();
                result.setUuid(entry.uuid);
                if (responseProperties != null) {
                    result.setParameters(new LinkedHashMap<>(responseProperties));
                } else {
                    result.setParameters(new LinkedHashMap<>());
                }
                if (entry.result.getStatus() != null) {
                    result.setStatus(entry.result.getStatus().name());
                }
                if (entry.result.getReason() != null) {
                    result.setReason(entry.result.getReason());
                }
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
    public void queueSendResult(String uuid, ServiceMonitorResponse result) {
        queue.asyncSend(new Entry(uuid, result));
    }

//========================================
// Downstream
//----------------------------------------

    private void stubConsumer(Entry entry) {
        try {
            if (log.isDebugEnabled()) {
                // TBD: REMOVE the json mapping - feed response back to Core
                log.debug("O-POLL STATUS: " + new ObjectMapper().writeValueAsString(entry.result));
            }
            consumer.accept(entry);
        } catch (JsonProcessingException jpExc) {
            log.warn("error processing workflow result", jpExc);
        }
    }

    static class Entry {
        String uuid;
        ServiceMonitorResponse result;

        public Entry(String uuid, ServiceMonitorResponse result) {
            this.uuid = uuid;
            this.result = result;
        }
    }
}
