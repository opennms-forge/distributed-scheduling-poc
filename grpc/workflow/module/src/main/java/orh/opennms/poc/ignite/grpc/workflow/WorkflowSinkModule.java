package orh.opennms.poc.ignite.grpc.workflow;

import org.opennms.horizon.ipc.sink.aggregation.IdentityAggregationPolicy;
import org.opennms.horizon.ipc.sink.api.AggregationPolicy;
import org.opennms.horizon.ipc.sink.api.AsyncPolicy;
import org.opennms.horizon.ipc.sink.api.SinkModule;
import org.opennms.poc.ignite.grpc.workflow.contract.WorkflowResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowSinkModule implements SinkModule<WorkflowResults, WorkflowResults> {

  private static final String MODULE_ID = "workflows";
  private final Logger logger = LoggerFactory.getLogger(WorkflowSinkModule.class);

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public int getNumConsumerThreads() {
    return 0;
  }

  @Override
  public byte[] marshal(WorkflowResults resultsMessage) {
    try {
      return resultsMessage.toByteArray();
    } catch (Exception e) {
      logger.warn("Error while marshalling message {}.", resultsMessage, e);
      return new byte[0];
    }
  }

  @Override
  public WorkflowResults unmarshal(byte[] bytes) {
    try {
      return WorkflowResults.parseFrom(bytes);
    } catch (Exception e) {
      logger.warn("Error while unmarshalling message.", e);
      return null;
    }
  }

  @Override
  public byte[] marshalSingleMessage(WorkflowResults resultsMessage) {
    return marshal(resultsMessage);
  }

  @Override
  public WorkflowResults unmarshalSingleMessage(byte[] bytes) {
    return unmarshal(bytes);
  }

  @Override
  public AggregationPolicy<WorkflowResults, WorkflowResults, ?> getAggregationPolicy() {
    return new IdentityAggregationPolicy<>();
  }

  @Override
  public AsyncPolicy getAsyncPolicy() {
    return new AsyncPolicy() {
      public int getQueueSize() {
        return 10;
      }

      public int getNumThreads() {
        return 10;
      }

      public boolean isBlockWhenFull() {
        return true;
      }
    };
  }

}
