package org.opennms.poc.ignite.worker.workflows;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkflowRepository {

    public List<Workflow> getWorkflows() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new ClassPathResource("workflows.json").getFile(), Workflows.class).getWorkflows();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
