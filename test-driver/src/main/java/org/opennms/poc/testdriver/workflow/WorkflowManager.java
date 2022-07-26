package org.opennms.poc.testdriver.workflow;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import org.opennms.poc.ignite.grpc.publisher.WorkflowPublisher;
import org.opennms.poc.ignite.model.workflows.Workflows;

public class WorkflowManager implements WorkflowPublisher {

  private final WorkflowPublisher delegate;
  private final Map<MinionKey, Workflows> workflows = new LinkedHashMap<>();

  public WorkflowManager(WorkflowPublisher delegate) {
    this.delegate = delegate;
  }

  public Workflows get(String location) {
    return getWorkflows(key -> key.getLocation().equals(location))
        .orElse(null);
  }

  public Workflows get(String location, String system) {
    return getWorkflows(key -> key.getLocation().equals(location) && key.getId().equals(system))
        .orElse(null);
  }

  private Optional<Workflows> getWorkflows(Predicate<MinionKey> predicate) {
    return workflows.entrySet().stream()
        .filter(entry -> predicate.test(entry.getKey()))
        .map(Entry::getValue)
        .findFirst();
  }

  @Override
  public void publish(Workflows twin) throws IOException {
    delegate.publish(twin);
  }

}
