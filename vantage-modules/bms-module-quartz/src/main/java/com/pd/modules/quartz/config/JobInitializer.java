package com.pd.modules.quartz.config;

import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import com.pd.modules.quartz.util.ScheduleUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Initialize scheduled jobs from database on application startup
 */
@Component
public class JobInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JobInitializer.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SysJobRepository jobRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Initializing scheduled jobs from database ===");

        try {
            // Step 1: Clean up orphaned Quartz triggers (triggers that don't have matching DB entries)
            cleanOrphanedTriggers();

            // Step 2: Get all active jobs from database
            List<SysJob> jobs = jobRepository.findAllActive();

            if (jobs.isEmpty()) {
                log.info("No active jobs found in database");
                return;
            }

            int loadedCount = 0;
            int failedCount = 0;

            for (SysJob job : jobs) {
                try {
                    // Always recreate the job (ensures clean state)
                    if (job.getCronExpression() != null && !job.getCronExpression().isEmpty()) {
                        ScheduleUtils.createScheduleJob(scheduler, job);
                        loadedCount++;
                        log.info("Scheduled job: {} (ID: {})", job.getJobName(), job.getJobId());
                    } else {
                        log.warn("Job {} has no cron expression, skipping", job.getJobName());
                    }
                } catch (Exception e) {
                    failedCount++;
                    log.error("Failed to schedule job: {} (ID: {})", job.getJobName(), job.getJobId(), e);
                }
            }

            log.info("=== Job initialization complete: {} loaded, {} failed ===", loadedCount, failedCount);

        } catch (Exception e) {
            log.error("Failed to initialize scheduled jobs", e);
        }
    }

    /**
     * Remove all Quartz triggers that don't have a matching entry in sys_job table.
     */
    private void cleanOrphanedTriggers() throws Exception {
        // Get all job keys from Quartz
        Set<JobKey> quartzJobKeys = new HashSet<>();
        for (String group : scheduler.getJobGroupNames()) {
            for (JobKey key : scheduler.getJobKeys(org.quartz.impl.matchers.GroupMatcher.jobGroupEquals(group))) {
                quartzJobKeys.add(key);
            }
        }

        // Get all job IDs from database
        Set<JobKey> dbJobKeys = new HashSet<>();
        List<SysJob> dbJobs = jobRepository.findAllActive();
        for (SysJob job : dbJobs) {
            dbJobKeys.add(ScheduleUtils.getJobKey(job.getJobId(), job.getJobGroup()));
        }

        // Remove orphaned triggers
        int removedCount = 0;
        for (JobKey key : quartzJobKeys) {
            if (!dbJobKeys.contains(key)) {
                try {
                    scheduler.deleteJob(key);
                    removedCount++;
                    log.info("Removed orphaned Quartz trigger: {}", key);
                } catch (Exception e) {
                    log.warn("Failed to remove orphaned trigger: {}", key, e);
                }
            }
        }

        if (removedCount > 0) {
            log.info("Cleaned up {} orphaned Quartz trigger(s)", removedCount);
        } else {
            log.info("No orphaned Quartz triggers found");
        }
    }
}
