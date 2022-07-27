package org.opennms.poc.testdriver.rest;

import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.poc.testdriver.testtcpconnector.TestTcpConnectionServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/tester")
@RestController
public class TestDriverRestController {

    @Autowired
    private GrpcIpcServer grpcIpcServer;

    @Autowired
    private TestTcpConnectionServer testTcpConnectionServer;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello";
    }

    /**
     * Change the default TEST TCP CONNECTION schedule for sending back samples.
     *
     * @param schedule new schedule; may be a single number (milliseconds between samples) or a cron expression (using
     *                 Quartz cron syntax).
     * @return
     */
    @PutMapping("/test-tcp-connection/schedule")
    public String configureTestTcpConnectionSchedule(@RequestBody String schedule) {
        testTcpConnectionServer.setDefaultSchedule(schedule);

        return "configured schedule for new test-tcp connections: " + schedule;
    }

    @DeleteMapping("/test-tcp-connection/{id}")
    public String stopTestTcpConnection(@PathVariable("id") int id) {
        boolean result = testTcpConnectionServer.stopConnection(id);
        if (result) {
            return "Stopped";
        } else {
            return "Not Found";
        }
    }
}
