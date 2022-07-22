package org.opennms.poc.ignite.grpc.subscriber;

import java.util.function.Consumer;
import org.opennms.poc.ignite.model.workflows.Results;
import org.opennms.horizon.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.horizon.ipc.sink.api.SyncDispatcher;
import orh.opennms.poc.ignite.grpc.workflow.WorkflowSinkModule;
import orh.opennms.poc.ignite.grpc.workflow.WrapperMessage;

public class ResultsConsumer implements Consumer<Results> {

  private final SyncDispatcher<WrapperMessage<Results>> dispatcher;

  public ResultsConsumer(MessageDispatcherFactory messageDispatcherFactory) {
    dispatcher = messageDispatcherFactory.createSyncDispatcher(new WorkflowSinkModule());
  }

  @Override
  public void accept(Results results) {
    dispatcher.send(new WrapperMessage<>(results));
  }

}
