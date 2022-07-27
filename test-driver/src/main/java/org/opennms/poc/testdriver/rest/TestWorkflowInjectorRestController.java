package org.opennms.poc.testdriver.rest;

import org.opennms.poc.ignite.grpc.publisher.WorkflowPublisher;
import org.opennms.poc.ignite.model.workflows.Workflows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@RestController
@RequestMapping(value = "/injector/workflow", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
public class TestWorkflowInjectorRestController {

    private final WorkflowPublisher publisher;

    public TestWorkflowInjectorRestController(@Qualifier("manager") WorkflowPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public void inject(@RequestBody Workflows model) {
        try {
            publisher.publish(model);
        } catch (IOException e) {
            throw new WebApplicationException("Failed to inject data", Response.serverError().entity(e).build());
        }
    }
}
