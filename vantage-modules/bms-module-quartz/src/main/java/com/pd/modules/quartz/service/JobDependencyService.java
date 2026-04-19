package com.pd.modules.quartz.service;

import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Service for handling job dependencies and triggering dependent jobs
 */
@Service
public class JobDependencyService {

    private static final Logger log = LoggerFactory.getLogger(JobDependencyService.class);

    private final SysJobRepository jobRepository;
    private final Scheduler scheduler;

    public JobDependencyService(SysJobRepository jobRepository, Scheduler scheduler) {
        this.jobRepository = jobRepository;
        this.scheduler = scheduler;
    }

    /**
     * Trigger dependent jobs after a job completes successfully
     */
    public void triggerDependentJobs(SysJob completedJob) {
        if (completedJob.getDependentJobIds() == null || completedJob.getDependentJobIds().isEmpty()) {
            return;
        }

        try {
            String[] dependentJobIds = completedJob.getDependentJobIds().split(",");
            
            for (String jobIdStr : dependentJobIds) {
                try {
                    Long dependentJobId = Long.parseLong(jobIdStr.trim());
                    triggerJob(dependentJobId, completedJob);
                } catch (NumberFormatException e) {
                    log.warn("Invalid dependent job ID: {}", jobIdStr);
                }
            }
        } catch (Exception e) {
            log.error("Failed to trigger dependent jobs for job {}", completedJob.getJobId(), e);
        }
    }

    /**
     * Trigger a specific job
     */
    private void triggerJob(Long jobId, SysJob parentJob) throws SchedulerException {
        SysJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Dependent job not found: " + jobId));

        // Only trigger if job is active
        if ("0".equals(job.getStatus())) {
            JobKey jobKey = new JobKey("TASK_" + jobId, job.getJobGroup());
            
            // Create job data map with parent job info
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("PARENT_JOB_ID", parentJob.getJobId());
            dataMap.put("PARENT_JOB_NAME", parentJob.getJobName());
            
            scheduler.triggerJob(jobKey, dataMap);
            log.info("Triggered dependent job {} (parent: {})", jobId, parentJob.getJobId());
        } else {
            log.info("Skipping dependent job {} as it is not active", jobId);
        }
    }

    /**
     * Check if a job can be executed (considering dependencies)
     */
    public boolean canExecuteJob(SysJob job) {
        // Check if today is a holiday and job is not allowed on holidays
        if (job.getAllowHoliday() != null && !job.getAllowHoliday()) {
            // Holiday check logic can be added here
            // For now, we'll just return true
            log.debug("Holiday check passed for job {}", job.getJobId());
        }
        
        return true;
    }

    /**
     * Get all jobs that depend on the given job
     */
    public Iterable<SysJob> findDependentJobs(Long jobId) {
        return jobRepository.findAll().stream()
                .filter(job -> {
                    if (job.getDependentJobIds() == null) return false;
                    return Arrays.stream(job.getDependentJobIds().split(","))
                            .anyMatch(id -> id.trim().equals(jobId.toString()));
                })
                .toList();
    }
}
