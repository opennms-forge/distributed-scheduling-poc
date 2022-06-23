package org.opennms.poc.ignite.worker.workflows;

import java.util.LinkedList;
import java.util.List;

public class Workflows {

    private List<Workflow> workflows = new LinkedList<>();

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<Workflow> workflows) {
        this.workflows = workflows;
    }
}
