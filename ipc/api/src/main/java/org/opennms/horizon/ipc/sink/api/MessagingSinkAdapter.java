package org.opennms.horizon.ipc.sink.api;

import com.google.protobuf.Message;
import org.opennms.cloud.grpc.minion.SinkMessage;

public class MessagingSinkAdapter<S extends Message, T extends Message> implements MessagingModule<SinkMessage, SinkMessage> {

  private final SinkModule<S, T> delegate;

  public MessagingSinkAdapter(SinkModule<S, T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getNumConsumerThreads() {
    return delegate.getNumConsumerThreads();
  }

  @Override
  public byte[] marshal(SinkMessage message) {
    return message.getContent().toByteArray();
  }

  @Override
  public SinkMessage unmarshal(byte[] message) {
    return null;
  }

  @Override
  public byte[] marshalSingleMessage(SinkMessage message) {
    return new byte[0];
  }

  @Override
  public SinkMessage unmarshalSingleMessage(byte[] message) {
    return null;
  }

  @Override
  public AggregationPolicy<SinkMessage, SinkMessage, ?> getAggregationPolicy() {
    AggregationPolicy<S, T, ?> aggregationPolicy = delegate.getAggregationPolicy();
    return new AggregationPolicy<SinkMessage, SinkMessage, Object>() {
      @Override
      public int getCompletionSize() {
        return aggregationPolicy.getCompletionSize();
      }

      @Override
      public int getCompletionIntervalMs() {
        return aggregationPolicy.getCompletionIntervalMs();
      }

      @Override
      public Object key(SinkMessage message) {
        return aggregationPolicy.key(message);
      }

      @Override
      public Object aggregate(Object accumulator, SinkMessage newMessage) {
        return aggregationPolicy.aggregate(accumulator, newMessage);
      }

      @Override
      public SinkMessage build(Object accumulator) {
        return null;
      }
    };
  }

  @Override
  public AsyncPolicy getAsyncPolicy() {
    return delegate.getAsyncPolicy();
  }
}
