package org.opennms.poc.testdriver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main program for the test-driver.  Delegates to SpringApplication to do the heavy lifting.
 */
@SpringBootApplication
public class TestDriverMain {
    public static void main(String[] args) {
        SpringApplication.run(TestDriverMain.class, args);
    }
}
