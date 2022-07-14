package org.opennms.poc.scheduler.impl;

import lombok.Getter;
import lombok.Setter;
import org.opennms.poc.scheduler.OpennmsScheduler;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DEVELOPER NOTE:
 *  Quartz does not have a simple "pass a runnable to execute" interface because it is designed heavily around
 *  serialization.  This leads to the following:
 *
 *      - runnableMap - keep track of the runnable by task id
 *      - MyQuartzJob - a simple class that bridges the "real" task via lookup
 */
public class OpennmsSchedulerImpl implements OpennmsScheduler {

    public static final String QUARTZ_TASK_GROUP = "opennms.scheduler.task";
    public static final String QUARTZ_TRIGGER_GROUP = "opennms.scheduler.trigger";
    public static final String QUARTZ_TRIGGER_NAME_PREFIX = "opennms.scheduler.trigger.";
    public static final String QUARTZ_SCHEDULER_NAME = "opennms.quartz-scheduler";
    public static final int DEFAULT_THREAD_COUNT = 10;

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(OpennmsSchedulerImpl.class);

    private Logger log = DEFAULT_LOGGER;

    @Getter
    @Setter
    private int threadCount = DEFAULT_THREAD_COUNT;

    private Scheduler scheduler;
    private Map<String, Runnable> runnableMap = new HashMap<>();


//========================================
// Lifecycle Management
//----------------------------------------

    public void init() {
        try {
            scheduler = prepareQuartzScheduler();
            scheduler.start();
        } catch (SchedulerException exc) {
            throw new RuntimeException("Failed to start up Quartz scheduler", exc);
        }
    }

    public void shutdown() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException exc) {
            throw new RuntimeException("Error on shutdown of Quartz scheduler", exc);
        }
    }

//========================================
// Operations
//----------------------------------------

    @Override
    public void scheduleTaskOnCron(String taskId, String cronExpression, Runnable operation) {
        JobDetail jobDetail =
                JobBuilder.newJob(MyQuartzJob.class)
                        .withIdentity(taskId, QUARTZ_TASK_GROUP)
                        .build();

        Trigger trigger = prepareCronTrigger(cronExpression);

        try {
            runnableMap.put(taskId, operation);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException scExc) {
            throw new RuntimeException("Scheduler error", scExc);
        }
    }

    @Override
    public void schedulePeriodically(String taskId, long period, TimeUnit unit, Runnable operation) {
        JobDetail jobDetail =
                JobBuilder.newJob(MyQuartzJob.class)
                        .withIdentity(taskId, QUARTZ_TASK_GROUP)
                        .build();

        Trigger trigger = preparePeriodicTrigger(taskId, period, unit);

        try {
            runnableMap.put(taskId, operation);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException scExc) {
            throw new RuntimeException("Scheduler error", scExc);
        }
    }

    @Override
    public void cancelTask(String taskId) {
        try {
            runnableMap.remove(taskId);
            scheduler.deleteJob(new JobKey(taskId, QUARTZ_TASK_GROUP));
        } catch (SchedulerException sExc) {
            throw new RuntimeException("Error deleting quartz job", sExc);
        }
    }

    //========================================
    // Internal Operations
    //----------------------------------------

    private Scheduler prepareQuartzScheduler() throws SchedulerException {
        ThreadPool threadPool = prepareQuartzThreadPool();
        DirectSchedulerFactory schedulerFactory = DirectSchedulerFactory.getInstance();

        schedulerFactory.createScheduler(QUARTZ_SCHEDULER_NAME, "instance1", threadPool, new RAMJobStore());
        return schedulerFactory.getScheduler(QUARTZ_SCHEDULER_NAME);
    }

    private ThreadPool prepareQuartzThreadPool() {
        return new SimpleThreadPool(threadCount, Thread.NORM_PRIORITY);
    }

    private Trigger prepareCronTrigger(String cronExpression) {
        // TODO: TimeZone or just use the default?
        return CronScheduleBuilder
                .cronSchedule(cronExpression)
                .build();
    }

    private Trigger preparePeriodicTrigger(String taskId, long period, TimeUnit timeUnit) {
        ScheduleBuilder<? extends Trigger> scheduleBuilder;
        if (timeUnit.toSeconds(period) < 1) {
            scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(1);
        } else {
            // Quartz only allows intervals in SECONDS, MINUTES, or HOURS
            // Note it also only supports int, but that should not be a problem as MAX_INT seconds = 68 years (and change)
            scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever((int) timeUnit.toMillis(period));
        }

        String triggerId = QUARTZ_TRIGGER_NAME_PREFIX + taskId;

        return TriggerBuilder.newTrigger()
                .withIdentity(triggerId, QUARTZ_TRIGGER_GROUP)
                .startNow()
                .withSchedule(scheduleBuilder)
                .build();
    }

//========================================
// Internal Classes
//----------------------------------------

    /**
     * Adapter class required by Quartz.
     */
    private static class MyQuartzJob implements Job {
        // Quartz requires an explicit no-arg constructor on the class.
        public MyQuartzJob() {
            // log.trace("new instance of MyQuartzJob");
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            String jobName = context.getJobDetail().getKey().getName();

            Runnable runnable;
            runnable = runnableMap.get(jobName);

            if (runnable != null) {
                runnable.run();
            } else {
                log.info("Have execution of job without runnable defined - may happen normally on job removal: job-name={}", jobName);
            }
        }
    }
}
