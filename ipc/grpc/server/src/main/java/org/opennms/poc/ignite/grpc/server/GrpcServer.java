package org.opennms.poc.ignite.grpc.server;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;

public interface GrpcServer {

  // one way for now
  void register(ModuleHandler handler);

  void start() throws IOException;
  void stop();

  CompletableFuture<ZonedDateTime> push(String systemId, String location, CloudToMinionMessage message);
  CompletableFuture<Void> broadcast(CloudToMinionMessage message);
  CompletableFuture<RpcResponseProto> request(String systemId, String location, RpcRequestProto request);

}
