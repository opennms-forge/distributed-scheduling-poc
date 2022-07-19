package org.opennms.poc.ignite.grpc.client.internal;

import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import java.io.IOException;
import java.util.List;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceBlockingStub;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceStub;
import org.opennms.horizon.core.identity.Identity;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.GrpcClientFactory;

// transformed from GrpcClientBuilder
public class DefaultGrpcClientFactory implements GrpcClientFactory {

  //private final Logger logger = LoggerFactory.getLogger(DefaultGrpcClientFactory.class);
  private final Identity identity;

  public DefaultGrpcClientFactory(Identity identity) {
    this.identity = identity;
  }

  @Override
  public GrpcClient create(String host, int port, List<ClientInterceptor> interceptors) throws Exception {
    ManagedChannel channel = getChannel(host, port, interceptors);

    CloudServiceStub asyncStub = CloudServiceGrpc.newStub(channel);
    CloudServiceBlockingStub blockingRpcStub = CloudServiceGrpc.newBlockingStub(channel);
    StubGrpcClient client = new StubGrpcClient(channel, asyncStub, blockingRpcStub, identity);
    return client;
  }

  private ManagedChannel getChannel(String host, int port, List<ClientInterceptor> interceptors) throws IOException {
    int maxInboundMessageSize = DEFAULT_MESSAGE_SIZE;
    NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
      .maxInboundMessageSize(maxInboundMessageSize)
      .intercept(new DelegatingClientInterceptor(interceptors))
      .keepAliveWithoutCalls(true);

    return channelBuilder.usePlaintext().build();
  }
}
