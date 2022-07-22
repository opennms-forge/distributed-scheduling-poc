package orh.opennms.poc.ignite.grpc.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opennms.horizon.ipc.sink.api.AggregationPolicy;
import org.opennms.horizon.ipc.sink.api.AsyncPolicy;
import org.opennms.horizon.ipc.sink.api.SinkModule;
import org.opennms.poc.ignite.model.workflows.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowSinkModule implements SinkModule<WrapperMessage<Results>, WrapperMessage<Results>> {

  private static final String MODULE_ID = "workflows";
  private final Logger logger = LoggerFactory.getLogger(WorkflowSinkModule.class);
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String getId() {
    return MODULE_ID;
  }

  @Override
  public int getNumConsumerThreads() {
    return 0;
  }

  @Override
  public byte[] marshal(WrapperMessage<Results> resultsMessage) {
    try {
      return objectMapper.writeValueAsBytes(resultsMessage.getMessage());
    } catch (Exception e) {
      logger.warn("Error while marshalling message {}.", resultsMessage, e);
      return new byte[0];
    }
  }

  @Override
  public WrapperMessage<Results> unmarshal(byte[] bytes) {
    try {
      Results results = objectMapper.readValue(bytes, Results.class);
      return new WrapperMessage<>(results);
    } catch (Exception e) {
      logger.warn("Error while unmarshalling message.", e);
      return null;
    }
  }

  @Override
  public byte[] marshalSingleMessage(WrapperMessage<Results> resultsMessage) {
    return marshal(resultsMessage);
  }

  @Override
  public WrapperMessage unmarshalSingleMessage(byte[] bytes) {
    return unmarshal(bytes);
  }

  @Override
  public AggregationPolicy<WrapperMessage<Results>, WrapperMessage<Results>, ?> getAggregationPolicy() {
    return null;
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
