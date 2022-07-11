package org.opennms.poc.ignite.grpc.whiteboard.internal;

import static org.opennms.poc.ignite.grpc.whiteboard.api.MessageListener.SUBSCRIBER_KEY;

import com.savoirtech.eos.pattern.whiteboard.AbstractWhiteboard;
import com.savoirtech.eos.util.ServiceProperties;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.poc.ignite.grpc.whiteboard.api.MessageListener;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcWhiteboard extends AbstractWhiteboard<MessageListener, Closeable> {

  private final Logger logger = LoggerFactory.getLogger(GrpcWhiteboard.class);
  private final TwinSubscriber twinSubscriber;

  public GrpcWhiteboard(BundleContext bundleContext, TwinSubscriber twinSubscriber) {
    super(bundleContext, MessageListener.class);
    this.twinSubscriber = twinSubscriber;
  }

  @Override
  protected Closeable addService(MessageListener service, ServiceProperties props) {
    Class<?> payload = service.getType();
    String subscriberKey = props.getProperty(SUBSCRIBER_KEY);

    if (payload == null) {
      logger.warn("Subscriber {} does not specify proper payload type, ignoring", service);
      return null;
    }
    if (subscriberKey == null) {
      logger.warn("Subscriber {} for payload {} does not specify proper subscriber key, ignoring", service, payload.getName());
      return null;
    }

    return twinSubscriber.subscribe(subscriberKey, payload, new Consumer() {
      @Override
      public void accept(Object message) {
        if (message != null && payload.isInstance(message)) {
          service.accept(message);
        } else {
          logger.warn("Message {} is not a valid object required by subscriber {}.", message, subscriberKey);
        }
      }

      @Override
      public String toString() {
        return "closeable consumer for " + service;
      }
    });
  }

  @Override
  protected void removeService(MessageListener service, Closeable tracked) {
    try {
      tracked.close();
    } catch (IOException e) {
      logger.warn("Error while closing up grpc subscription for {}", service, e);
    }
  }

}
