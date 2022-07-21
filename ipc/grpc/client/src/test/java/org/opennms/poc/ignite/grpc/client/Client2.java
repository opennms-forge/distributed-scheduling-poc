package org.opennms.poc.ignite.grpc.client;

import java.util.Properties;
import org.opennms.core.ipc.grpc.client.GrpcClientConstants;
import org.opennms.core.ipc.grpc.client.MinionGrpcClient;
import org.opennms.horizon.core.identity.IdentityImpl;
import org.opennms.horizon.ipc.sink.api.AggregationPolicy;
import org.opennms.horizon.ipc.sink.api.AsyncPolicy;
import org.opennms.horizon.ipc.sink.api.Message;
import org.opennms.horizon.ipc.sink.api.SinkModule;

public class Client2 {
  public static void main(String[] args) throws Exception {
    IdentityImpl identity = new IdentityImpl("minion01", "dc1", "minion");
    Properties properties = new Properties();
    properties.put(GrpcClientConstants.GRPC_PORT, 8080);
    MinionGrpcClient client = new MinionGrpcClient(identity, properties);
    client.start();

    SinkModule<Message, Message> module = new SinkModule<>() {
      @Override
      public String getId() {
        return "workflow";
      }

      @Override
      public int getNumConsumerThreads() {
        return 0;
      }

      @Override
      public byte[] marshal(Message message) {
        return new byte[0];
      }

      @Override
      public Message unmarshal(byte[] bytes) {
        return null;
      }

      @Override
      public byte[] marshalSingleMessage(Message message) {
        return new byte[0];
      }

      @Override
      public Message unmarshalSingleMessage(byte[] bytes) {
        return null;
      }

      @Override
      public AggregationPolicy<Message, Message, ?> getAggregationPolicy() {
        return null;
      }

      @Override
      public AsyncPolicy getAsyncPolicy() {
        return null;
      }
    };
    client.dispatch(module, null, null);

    System.in.read();
  }
}
