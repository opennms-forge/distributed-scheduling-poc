package org.opennms.poc.testdriver.config;

import org.opennms.poc.scheduler.OpennmsScheduler;
import org.opennms.poc.scheduler.impl.OpennmsSchedulerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
public class OpennmsSchedulerConfig {

    @Value("${opennms-test-scheduler.thread-count:3}")
    private int threadCount;

    @Bean
    public OpennmsScheduler prepareOpennmsScheduler() {
        OpennmsSchedulerImpl result = new OpennmsSchedulerImpl();
        result.setScheduledThreadPoolExecutor(new ScheduledThreadPoolExecutor(threadCount));

        return result;
    }
}
