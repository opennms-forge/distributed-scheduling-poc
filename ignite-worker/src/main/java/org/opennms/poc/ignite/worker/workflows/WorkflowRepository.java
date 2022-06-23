package org.opennms.poc.ignite.worker.workflows;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkflowRepository {

    public List<Workflow> getWorkflows() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ClassPathResource resource = new ClassPathResource("workflows.json");
            try (InputStream inputStream = resource.getInputStream()) {
                return objectMapper.readValue(inputStream, Workflows.class).getWorkflows();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
