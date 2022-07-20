package org.opennms.poc.ignite.grpc.server.internal;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceImplBase;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.Empty;
import org.opennms.cloud.grpc.minion.MinionHeader;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequest;
import org.opennms.cloud.grpc.minion.RpcResponse;
import org.opennms.poc.ignite.grpc.server.GrpcServer;
import org.opennms.poc.ignite.grpc.server.ModuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticModuleHandler extends CloudServiceImplBase implements ModuleHandler {

  private final Logger logger = LoggerFactory.getLogger(StaticModuleHandler.class);
  private final Set<CollectorModule<Message>> collectors = new LinkedHashSet<>();
  private final Set<PushModule<Message>> pushers = new LinkedHashSet<>();
  private final Set<IncomingRpcModule<Message, Message>> incoming = new LinkedHashSet<>();
  private final Set<OutgoingRpcModule> outgoing = new LinkedHashSet<>();
  private final Map<String, Set<StreamObserver<CloudToMinionMessage>>> pushChannels = new ConcurrentHashMap<>();
  private final Map<SessionKey, StreamObserver<RpcRequest>> rpcChannels = new ConcurrentHashMap<>();
  private final Map<String, RpcCall> rpcRequests = new ConcurrentHashMap<>();
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "server-publisher"));

  @Override
  public void start(GrpcServer server) {
    collectors.forEach(module -> module.start(server));
    pushers.forEach(module -> module.start(server));
    incoming.forEach(module -> module.start(server));
    outgoing.forEach(module -> module.start(server));
  }

  @Override
  public CompletableFuture<ZonedDateTime> push(String systemId, String location, CloudToMinionMessage message) {
    Stream<StreamObserver<CloudToMinionMessage>> stream;
    if (systemId == null && location == null) {
      stream = pushChannels.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream());
    } else {
      stream = pushChannels.get(sinkKey(systemId, location)).stream();
    }

    CompletableFuture<ZonedDateTime> future = new CompletableFuture<>();
    executor.execute(new Runnable() {
      @Override
      public void run() {
        stream.forEach(observer -> observer.onNext(message));
        future.complete(ZonedDateTime.now());
      }
    });
    return future;
  }

  @Override
  public CompletableFuture<RpcResponse> request(String systemId, String location, RpcRequest request) {
    String requestId = request.getRpcId();

    CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
    long timeout = System.currentTimeMillis() + (request.getExpirationTime() == 0 ? 100 : request.getExpirationTime());
    rpcRequests.put(requestId, new RpcCall(timeout, responseFuture));
    rpcChannels.entrySet().stream()
      .filter(entry -> systemId.equals(entry.getKey().systemId) && location.equals(entry.getKey().location))
      .map(Entry::getValue)
      .findFirst()
      .ifPresent(rpcChannel -> rpcChannel.onNext(request));

    executor.schedule(new Runnable() {
      @Override
      public void run() {
        RpcCall call = rpcRequests.remove(requestId);
        if (call != null) {
          call.response.completeExceptionally(new TimeoutException());
        }
      }
    }, timeout + 1, TimeUnit.MILLISECONDS);
    return responseFuture;
  }

  @Override
  public StreamObserver<RpcResponse> cloudToMinionRPC(StreamObserver<RpcRequest> responseObserver) {
    return new StreamObserver<>() {
      @Override
      public void onNext(RpcResponse value) {
        // initial ident request
        if ("MINION_HEADERS".equals(value.getModuleId())) {
          SessionKey sessionKey = new SessionKey(value.getSystemId(), value.getLocation());
          StreamObserver<RpcRequest> observer = rpcChannels.put(sessionKey, responseObserver);
          if (observer != null) {
            logger.info("Closing earlier rpc channel opened by minion {}", sessionKey);
            observer.onCompleted();
          } else {
            logger.info("New connection from minion {}", sessionKey);
          }
          return;
        }

        String rpcId = value.getRpcId();
        RpcCall future = rpcRequests.remove(rpcId);
        if (future == null) {
          logger.warn("Received unassociated rpc response {}", value);
          return;
        }
        future.response.complete(value);
      }

      @Override
      public void onError(Throwable t) {
        logger.error("Failure in rpc communication between cloud and minion", t);
      }

      @Override
      public void onCompleted() {
        SessionKey key = null;
        for (Entry<SessionKey, StreamObserver<RpcRequest>> entry : rpcChannels.entrySet()) {
          if (entry.equals(responseObserver)) {
            key = entry.getKey();
            break;
          }
        }
        if (key != null) {
          logger.info("Removing minion connection {}", key);
          rpcChannels.remove(key);
        }
      }
    };
  }

  @Override
  public void cloudToMinionMessages(MinionHeader request, StreamObserver<CloudToMinionMessage> responseObserver) {
    String key = sinkKey(request.getSystemId(), request.getLocation());
    if (!pushChannels.containsKey(key)) {
      pushChannels.put(key, Collections.synchronizedSet(new LinkedHashSet<>()));
    }
    pushChannels.get(key).add(responseObserver);
  }

  @Override
  public void minionToCloudRPC(RpcRequest request, StreamObserver<RpcResponse> responseObserver) {
    for (IncomingRpcModule<Message, Message> module : incoming) {
      Predicate<RpcRequest> predicate = module.predicate();
      if (predicate.test(request)) {
        if (!request.getRpcContent().is(module.receive())) {
          logger.warn("Unsupported payload {} detected for module {}", request.getRpcContent(), module);
          continue;
        }

        try {
          Message payload = request.getRpcContent().unpack(module.receive());
          module.handle(payload).whenComplete((response, error) -> {
            if (error != null) {
              responseObserver.onError(error);
              return;
            }
            Any replyContent = Any.pack(response);
            RpcResponse rpcResponse = RpcResponse.newBuilder()
              .setModuleId(request.getModuleId())
              .setRpcId(request.getRpcId())
              .setLocation(request.getLocation())
              .setSystemId(request.getSystemId())
              .setRpcContent(replyContent)
              .build();
            responseObserver.onNext(rpcResponse);
          });
        } catch (InvalidProtocolBufferException e) {
          logger.error("Could not deserialize payload of type {}", module.receive(), e);
        }
      }
    }
  }

  @Override
  public StreamObserver<MinionToCloudMessage> minionToCloudMessages(StreamObserver<Empty> responseObserver) {
    return new StreamObserver<>() {
      @Override
      public void onNext(MinionToCloudMessage value) {
        for (CollectorModule<Message> module : collectors) {
          Predicate<Message> payload = module.predicate();
          if (payload.test(value)) {
            module.handle(value);
          }
        }
      }

      @Override
      public void onError(Throwable t) {
        logger.error("Received failure for incoming communication", t);
      }

      @Override
      public void onCompleted() {
        logger.info("Closing incoming event connection");
      }
    };
  }


  @Override
  public <In extends Message> void registerCollector(CollectorModule<In> module) {
    this.collectors.add((CollectorModule<Message>) module);
  }

  @Override
  public <In extends Message> void unregisterCollector(CollectorModule<In> module) {
    this.collectors.remove(module);
  }

  @Override
  public <Out extends Message> void registerPush(PushModule<Out> module) {
    this.pushers.add((PushModule<Message>) module);
  }

  @Override
  public <Out extends Message> void unregisterPush(PushModule<Out> module) {
    this.pushers.remove(module);
  }

  @Override
  public <Local extends Message, Remote extends Message> void registerIncomingRpc(IncomingRpcModule<Local, Remote> module) {
    incoming.add((IncomingRpcModule<Message, Message>) module);
  }

  @Override
  public <Local extends Message, Remote extends Message> void unregisterIncomingRpc(IncomingRpcModule<Local, Remote> module) {
    incoming.remove(module);
  }

  @Override
  public void registerOutgoingRpc(OutgoingRpcModule module) {
    outgoing.add((OutgoingRpcModule) module);
  }

  @Override
  public void unregisterOutgoingRpc(OutgoingRpcModule module) {
    outgoing.remove(module);
  }

  private String sinkKey(String systemId, String location) {
    return systemId + "@" + location;
  }

  static class SessionKey {
    String systemId;
    String location;

    public SessionKey(String systemId, String location) {
      this.systemId = systemId;
      this.location = location;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof SessionKey)) {
        return false;
      }
      SessionKey that = (SessionKey) o;
      return Objects.equals(systemId, that.systemId) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
      return Objects.hash(systemId, location);
    }

    public String toString() {
      return systemId + "@" + location;
    }
  }

  static class RpcCall {
    long timeout;
    CompletableFuture<RpcResponse> response;

    public RpcCall(long timeout, CompletableFuture<RpcResponse> response) {
      this.timeout = timeout;
      this.response = response;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof RpcCall)) {
        return false;
      }
      RpcCall rpcCall = (RpcCall) o;
      return timeout == rpcCall.timeout && Objects.equals(response, rpcCall.response);
    }

    @Override
    public int hashCode() {
      return Objects.hash(timeout, response);
    }
  }

}
