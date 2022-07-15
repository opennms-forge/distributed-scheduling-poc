package org.opennms.poc.ignite.grpc.client;

import io.grpc.ClientInterceptor;
import java.util.List;
import java.util.Properties;

public interface GrpcClientFactory {

  GrpcClient create(Properties properties, List<ClientInterceptor> interceptors) throws Exception;

}
