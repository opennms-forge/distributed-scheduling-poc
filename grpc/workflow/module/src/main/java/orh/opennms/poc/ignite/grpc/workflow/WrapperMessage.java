package orh.opennms.poc.ignite.grpc.workflow;

import java.util.Objects;
import org.opennms.horizon.ipc.sink.api.Message;

public class WrapperMessage<T> implements Message {

  private final T message;

  public WrapperMessage(T message) {
    this.message = message;
  }

  public T getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WrapperMessage)) {
      return false;
    }
    WrapperMessage<?> that = (WrapperMessage<?>) o;
    return Objects.equals(getMessage(), that.getMessage());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMessage());
  }

  public String toString() {
    return "WrapperMessage [" + message + "]";
  }

}