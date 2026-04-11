package com.pd.modules.quartz.util;

import org.quartz.*;
import com.pd.modules.quartz.domain.SysJob;

/**
 * Quartz job scheduling utility
 */
public class ScheduleUtils {

    private static final String JOB_NAME_PREFIX = "task_";

    /**
     * Get job key
     */
    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return new JobKey(getJobName(jobId), jobGroup);
    }

    /**
     * Get trigger key
     */
    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return new TriggerKey(getJobName(jobId), jobGroup);
    }

    /**
     * Get job name
     */
    public static String getJobName(Long jobId) {
        return JOB_NAME_PREFIX + jobId;
    }

    /**
     * Create schedule job
     */
    public static void createScheduleJob(Scheduler scheduler, SysJob job) {
        try {
            Class<? extends Job> jobClass = QuartzTaskExecutor.class;
            
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(getJobKey(job.getJobId(), job.getJobGroup()))
                    .build();

            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(getTriggerKey(job.getJobId(), job.getJobGroup()))
                    .withSchedule(cronScheduleBuilder)
                    .build();

            jobDetail.getJobDataMap().put("TASK_PROPERTIES", job);

            if (scheduler.checkExists(getJobKey(job.getJobId(), job.getJobGroup()))) {
                scheduler.rescheduleJob(getTriggerKey(job.getJobId(), job.getJobGroup()), trigger);
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }

            if ("1".equals(job.getStatus())) {
                scheduler.pauseJob(getJobKey(job.getJobId(), job.getJobGroup()));
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to create schedule job", e);
        }
    }
}
