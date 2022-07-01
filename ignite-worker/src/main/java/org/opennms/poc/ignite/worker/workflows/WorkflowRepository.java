package org.opennms.poc.ignite.worker.workflows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


import com.fasterxml.jackson.databind.ObjectMapper;

public class WorkflowRepository {

    public List<Workflow> getWorkflows() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {

            InputStream stream = this.getClass().getClassLoader().getResourceAsStream("workflows.json");
            if (stream == null) {
                throw new FileNotFoundException("Test file does not exist.");
            }
            else {
                    return objectMapper.readValue(stream, Workflows.class).getWorkflows();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
