package org.opennms.poc.ignite.grpc.server.internal;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugObserver<T> implements StreamObserver<T> {

  private final Logger logger = LoggerFactory.getLogger(DebugObserver.class);
  private final StreamObserver<T> delegate;

  public DebugObserver(StreamObserver<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onNext(T value) {
    logger.debug("Pushing update {}", value);
    delegate.onNext(value);
  }

  @Override
  public void onError(Throwable t) {
    delegate.onError(t);
  }

  @Override
  public void onCompleted() {
    delegate.onCompleted();
  }
}
