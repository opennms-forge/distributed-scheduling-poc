package org.opennms.poc.testdriver.workflow;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.opennms.poc.ignite.grpc.publisher.WorkflowPublisher;
import org.opennms.poc.ignite.model.workflows.Workflows;
import org.osgi.service.component.annotations.Component;

public class WorkflowManager implements WorkflowPublisher {

  private final WorkflowPublisher delegate;
  private final Map<MinionKey, Set<Workflows>> workflows = new LinkedHashMap<>();

  public WorkflowManager(WorkflowPublisher delegate) {
    this.delegate = delegate;
  }

  public Set<Workflows> get(String location) {
    return getWorkflows(key -> key.getLocation().equals(location))
      .orElse(Collections.emptySet());
  }

  public Set<Workflows> get(String location, String system) {
    return getWorkflows(key -> key.getLocation().equals(location) && key.getId().equals(system))
      .orElse(Collections.emptySet());
  }

  public Set<Workflows> getAll() {
    return workflows.values().stream()
      .flatMap(Collection::stream)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public void publish(Workflows twin) throws IOException {
    if (twin.getSystemId() == null && twin.getLocation() == null) {
      store(MinionKey.NULL_KEY, twin);
    } else {
      MinionKey key = new MinionKey(twin.getSystemId(), twin.getLocation());
      store(key, twin);
    }
    delegate.publish(twin);
  }

  private Optional<Set<Workflows>> getWorkflows(Predicate<MinionKey> predicate) {
    return workflows.entrySet().stream()
      .filter(entry -> predicate.test(entry.getKey()))
      .map(Entry::getValue)
      .findFirst();
  }

  private void store(MinionKey key, Workflows twin) {
    if (!workflows.containsKey(key)) {
      workflows.put(key, new LinkedHashSet<>());
    }
    workflows.get(key).add(twin);
  }

}
