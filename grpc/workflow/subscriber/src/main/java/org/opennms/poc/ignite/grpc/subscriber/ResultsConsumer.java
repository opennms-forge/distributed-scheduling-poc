package org.opennms.poc.ignite.grpc.subscriber;

import com.google.protobuf.Any;
import com.google.protobuf.NullValue;
import com.google.protobuf.Value;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults.Builder;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults.WorkflowResult;
import org.opennms.poc.ignite.model.workflows.Result;
import org.opennms.poc.ignite.model.workflows.Results;
import org.opennms.horizon.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.horizon.ipc.sink.api.SyncDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.poc.ignite.grpc.workflow.WorkflowSinkModule;

public class ResultsConsumer implements Consumer<Results> {

  private final Logger logger = LoggerFactory.getLogger(ResultsConsumer.class);
  private final SyncDispatcher<WorkflowResults> dispatcher;

  public ResultsConsumer(MessageDispatcherFactory messageDispatcherFactory) {
    dispatcher = messageDispatcherFactory.createSyncDispatcher(new WorkflowSinkModule());
  }

  @Override
  public void accept(Results results) {
    Builder workflowBuilder = WorkflowResults.newBuilder();
    for (Result result : results.getResults()) {
      WorkflowResult.Builder resultBuilder = WorkflowResult.newBuilder();

      if (result.getParameters() != null) {
        for (Map.Entry<String, Object> entry : result.getParameters().entrySet()) {
          Object value = entry.getValue();
          Value.Builder valueBuilder = Value.newBuilder();
          if (value instanceof Number) {
            valueBuilder.setNumberValue(((Number) value).doubleValue());
          } else if (value instanceof String) {
            valueBuilder.setStringValue((String) value);
          } else {
            logger.warn("Unsupported result property {} {}", entry.getKey(), entry.getValue());
            valueBuilder.setNullValue(NullValue.NULL_VALUE);
          }
          resultBuilder.putParameters(entry.getKey(), Any.pack(valueBuilder.build()));
        }
      }
      Optional.ofNullable(result.getUuid()).ifPresent(resultBuilder::setUuid);
      Optional.ofNullable(result.getStatus()).ifPresent(resultBuilder::setStatus);
      Optional.ofNullable(result.getReason()).ifPresent(resultBuilder::setReason);
      workflowBuilder.addResults(resultBuilder.build());
    }
    dispatcher.send(workflowBuilder.build());
  }

}
