package org.opennms.poc.ignite.grpc.client.internal;

import com.google.common.base.Strings;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.net.ssl.SSLException;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceBlockingStub;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc.CloudServiceStub;
import org.opennms.horizon.core.identity.Identity;
import org.opennms.poc.ignite.grpc.client.GrpcClient;
import org.opennms.poc.ignite.grpc.client.GrpcClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// transformed from GrpcClientBuilder
public class DefaultGrpcClientFactory implements GrpcClientFactory {

  public static final String GRPC_CLIENT_PID = "org.opennms.core.ipc.grpc.client";
  public static final String GRPC_SERVER_PID = "org.opennms.core.ipc.grpc.server";
  public static final String LOG_PREFIX = "ipc";
  public static final String GRPC_HOST = "host";
  public static final String DEFAULT_GRPC_HOST = "localhost";
  public static final String GRPC_PORT = "port";
  public static final int DEFAULT_TWIN_GRPC_PORT = 8991;
  public static final String TLS_ENABLED = "tls.enabled";
  public static final String GRPC_MAX_INBOUND_SIZE = "max.message.size";
  public static final int DEFAULT_MESSAGE_SIZE = 10485760;

  public static final String CLIENT_CERTIFICATE_FILE_PATH = "client.cert.filepath";
  public static final String CLIENT_PRIVATE_KEY_FILE_PATH = "client.private.key.filepath";
  public static final String TRUST_CERTIFICATE_FILE_PATH = "trust.cert.filepath";

  public static final String SERVER_CERTIFICATE_FILE_PATH = "server.cert.filepath";
  public static final String PRIVATE_KEY_FILE_PATH = "server.private.key.filepath";

  private final Logger logger = LoggerFactory.getLogger(DefaultGrpcClientFactory.class);
  private final Identity identity;

  public DefaultGrpcClientFactory(Identity identity) {
    this.identity = identity;
  }

  @Override
  public GrpcClient create(Properties properties, List<ClientInterceptor> interceptors) throws Exception {
    ManagedChannel channel = getChannel(properties, interceptors);

    CloudServiceStub asyncStub = CloudServiceGrpc.newStub(channel);
    CloudServiceBlockingStub rpcStub = CloudServiceGrpc.newBlockingStub(channel);
    StubGrpcClient client = new StubGrpcClient(channel, asyncStub, rpcStub, identity);
    return client;
  }


  private SslContextBuilder buildSslContext(Properties properties) throws SSLException {
    SslContextBuilder builder = GrpcSslContexts.forClient();
    String clientCertChainFilePath = properties.getProperty(CLIENT_CERTIFICATE_FILE_PATH);
    String clientPrivateKeyFilePath = properties.getProperty(CLIENT_PRIVATE_KEY_FILE_PATH);
    String trustCertCollectionFilePath = properties.getProperty(TRUST_CERTIFICATE_FILE_PATH);

    if (!Strings.isNullOrEmpty(trustCertCollectionFilePath)) {
      builder.trustManager(new File(trustCertCollectionFilePath));
    }
    if (!Strings.isNullOrEmpty(clientCertChainFilePath) && !Strings.isNullOrEmpty(clientPrivateKeyFilePath)) {
      builder.keyManager(new File(clientCertChainFilePath), new File(clientPrivateKeyFilePath));
    } else if (!Strings.isNullOrEmpty(clientCertChainFilePath) || !Strings.isNullOrEmpty(clientPrivateKeyFilePath)) {
      logger.error("Only one of the required file paths were provided, need both client cert and client private key");
    }
    return builder;
  }

  private ManagedChannel getChannel(Properties properties, List<ClientInterceptor> interceptors) throws IOException {
    int port = PropertiesUtils.getProperty(properties, GRPC_PORT, DEFAULT_TWIN_GRPC_PORT);
    String host = PropertiesUtils.getProperty(properties, GRPC_HOST, DEFAULT_GRPC_HOST);
    int maxInboundMessageSize = PropertiesUtils.getProperty(properties, GRPC_MAX_INBOUND_SIZE, DEFAULT_MESSAGE_SIZE);
    NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
      .maxInboundMessageSize(maxInboundMessageSize)
      .intercept(new DelegatingClientInterceptor(interceptors))
      .keepAliveWithoutCalls(true);

    boolean tlsEnabled = Boolean.parseBoolean(properties.getProperty(TLS_ENABLED));
    if (tlsEnabled) {
      return channelBuilder
        .negotiationType(NegotiationType.TLS)
        .sslContext(buildSslContext(properties).build())
        .build();
    } else {
      return channelBuilder.usePlaintext().build();
    }
  }

  private SslContextBuilder getSslContextBuilder(Properties properties) {
    String certChainFilePath = properties.getProperty(SERVER_CERTIFICATE_FILE_PATH);
    String privateKeyFilePath = properties.getProperty(PRIVATE_KEY_FILE_PATH);
    String trustCertCollectionFilePath = properties.getProperty(TRUST_CERTIFICATE_FILE_PATH);
    if (Strings.isNullOrEmpty(certChainFilePath) || Strings.isNullOrEmpty(privateKeyFilePath)) {
      return null;
    }

    SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(new File(certChainFilePath), new File(privateKeyFilePath));
    if (!Strings.isNullOrEmpty(trustCertCollectionFilePath)) {
      sslClientContextBuilder.trustManager(new File(trustCertCollectionFilePath));
      sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE);
    }
    return GrpcSslContexts.configure(sslClientContextBuilder, SslProvider.OPENSSL);
  }

}
