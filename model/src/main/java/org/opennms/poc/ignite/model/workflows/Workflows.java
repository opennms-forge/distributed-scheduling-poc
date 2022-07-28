package org.opennms.poc.ignite.model.workflows;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Workflows {

    private List<Workflow> workflows = new LinkedList<>();

    private String location;
    private String systemId;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<Workflow> workflows) {
        this.workflows = workflows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Workflows)) {
            return false;
        }
        Workflows workflows1 = (Workflows) o;
        return Objects.equals(getWorkflows(), workflows1.getWorkflows())
            && Objects.equals(getLocation(), workflows1.getLocation())
            && Objects.equals(getSystemId(), workflows1.getSystemId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWorkflows(), getLocation(), getSystemId());
    }
}
