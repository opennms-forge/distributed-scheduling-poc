package org.opennms.poc.scheduler.impl;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.Getter;
import lombok.Setter;
import org.opennms.poc.scheduler.OpennmsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class OpennmsSchedulerImpl implements OpennmsScheduler {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(OpennmsSchedulerImpl.class);

    private Logger log = DEFAULT_LOGGER;

    // Prepare the parser.  Note that the "CRON DEFINITION" specifies WHICH FIELDS (and variations on field inputs) are supported.
    // For now, just use the QUARTZ setting
    private CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

    @Getter
    @Setter
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private Map<String, Runnable> scheduledTasks = new ConcurrentHashMap<>();

//========================================
// Lifecycle Management
//----------------------------------------

    public void shutdown() {
        log.info("Shutting down scheduler");
        scheduledThreadPoolExecutor.shutdownNow();
    }

//========================================
// Operations
//----------------------------------------

    @Override
    public void scheduleTaskOnCron(String taskId, String cronExpression, Runnable operation) {
        Cron cron = cronParser.parse(cronExpression);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        RecurringCronRunner runner = new RecurringCronRunner(executionTime, operation);

        // Remember the task before executing to avoid a possible race condition on reschedule
        Runnable old = scheduledTasks.put(taskId, runner);
        if (old != null) {
            log.debug("replacing existing operation for task: task-id={}", taskId);
            scheduledThreadPoolExecutor.remove(old);
        }

        // Trigger the next run.  After that, it will reschedule itself.
        scheduleNextRun(executionTime, runner);
    }

    @Override
    public void schedulePeriodically(String taskId, long period, TimeUnit unit, Runnable operation) {
        Runnable old = scheduledTasks.put(taskId, operation);
        if (old != null) {
            log.debug("replacing existing operation for task: task-id={}", taskId);
            scheduledThreadPoolExecutor.remove(old);
        }

        scheduledThreadPoolExecutor.scheduleAtFixedRate(operation, period, period, unit);
    }

    @Override
    public void scheduleOnce(String taskId, long period, TimeUnit unit, Runnable operation) {
        Runnable old = scheduledTasks.put(taskId, operation);
        if (old != null) {
            log.debug("replacing existing operation for task: task-id={}", taskId);
            scheduledThreadPoolExecutor.remove(old);
        }

        // Use a wrapper to remove the task from scheduledTasks once it completes.
        OneShotTaskWrapper wrapper = new OneShotTaskWrapper(taskId, operation);
        scheduledThreadPoolExecutor.schedule(wrapper, period, unit);
    }

    @Override
    public void cancelTask(String taskId) {
        Runnable runnable = scheduledTasks.remove(taskId);

        if (runnable != null) {
            if (runnable instanceof RecurringCronRunner) {
                // Tell the recurring runner to stop too
                ((RecurringCronRunner) runnable).shutdown();
            }

            scheduledThreadPoolExecutor.remove(runnable);
        }
    }

//========================================
// Internal Operations
//----------------------------------------

    private void scheduleNextRun(ExecutionTime executionTime, Runnable operation) {
        ZonedDateTime now = ZonedDateTime.now();

        // Get an Optional, but the contract says it will never be null...
        Optional<Duration> durationOpt = executionTime.timeToNextExecution(now);
        Duration duration = durationOpt.get();

        scheduledThreadPoolExecutor.schedule(operation, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

//========================================
// Internal Classes
//----------------------------------------

    private class RecurringCronRunner implements Runnable {
        private final ExecutionTime executionTime;
        private final Runnable operation;
        private boolean shutdown = false;

        public RecurringCronRunner(ExecutionTime executionTime, Runnable operation) {
            this.executionTime = executionTime;
            this.operation = operation;
        }

        @Override
        public void run() {
            if (!shutdown) {
                try {
                    log.debug("Executing now");
                    operation.run();
                } catch (Exception exc) {
                    log.warn("task failure", exc);
                } finally {
                    if (! shutdown) {
                        log.debug("Scheduling next execution");
                        scheduleNextRun(executionTime, this);
                    } else {
                        log.debug("NOT scheduling next execution; was shutdown");
                    }
                }
            } else {
                log.debug("Skipping execution and NOT scheduling next one; was shutdown");
            }
        }

        public void shutdown() {
            shutdown = true;
        }
    }

    private class OneShotTaskWrapper implements Runnable {

        private final String taskId;
        private final Runnable nested;

        public OneShotTaskWrapper(String taskId, Runnable nested) {
            this.taskId = taskId;
            this.nested = nested;
        }

        @Override
        public void run() {
            try {
                nested.run();
            } finally {
                scheduledTasks.remove(taskId);
            }
        }
    }
}
