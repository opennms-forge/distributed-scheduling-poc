package org.opennms.poc.ignite.model.workflows;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Result {

  private String uuid;
  private Map<String, Object> parameters = new LinkedHashMap<>();

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Result)) {
      return false;
    }
    Result result = (Result) o;
    return Objects.equals(uuid, result.uuid) && Objects.equals(parameters,
        result.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, parameters);
  }

  public String toString() {
    return "Result [" + uuid + " " + parameters + "]";
  }

}
