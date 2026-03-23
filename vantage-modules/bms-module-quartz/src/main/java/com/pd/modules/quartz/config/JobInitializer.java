package com.pd.modules.quartz.config;

import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import com.pd.modules.quartz.util.ScheduleUtils;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

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
            // Get all active jobs from database
            List<SysJob> jobs = jobRepository.findAllActive();
            
            if (jobs.isEmpty()) {
                log.info("No active jobs found in database");
                return;
            }
            
            int loadedCount = 0;
            int failedCount = 0;
            
            for (SysJob job : jobs) {
                try {
                    // Check if job is already scheduled
                    if (scheduler.checkExists(ScheduleUtils.getJobKey(job.getJobId(), job.getJobGroup()))) {
                        log.debug("Job {} already exists in scheduler, skipping", job.getJobName());
                        continue;
                    }
                    
                    // Schedule the job
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
}
