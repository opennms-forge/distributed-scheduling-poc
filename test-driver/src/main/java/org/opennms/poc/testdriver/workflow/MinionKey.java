package org.opennms.poc.testdriver.workflow;

import java.util.Objects;

public class MinionKey {

  private final String id;
  private final String location;

  public MinionKey(String id, String location) {
    this.id = id;
    this.location = location;
  }

  public String getId() {
    return id;
  }

  public String getLocation() {
    return location;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MinionKey)) {
      return false;
    }
    MinionKey minionKey = (MinionKey) o;
    return Objects.equals(getId(), minionKey.getId()) && Objects.equals(
        getLocation(), minionKey.getLocation());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getLocation());
  }
}
