package org.opennms.poc.testdriver.rest;

import org.opennms.core.grpc.common.GrpcIpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/tester")
@RestController
public class TestDriverRestController {

    @Autowired
    private GrpcIpcServer grpcIpcServer;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello";
    }
}
