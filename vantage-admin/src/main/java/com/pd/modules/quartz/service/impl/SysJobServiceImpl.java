package com.pd.modules.quartz.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.domain.SysJobLog;
import com.pd.modules.quartz.infrastructure.repository.SysJobLogRepository;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import com.pd.modules.quartz.service.ISysJobService;
import com.pd.modules.quartz.service.JobWebSocketService;
import com.pd.modules.quartz.util.CronUtils;
import com.pd.modules.quartz.util.ScheduleUtils;

/**
 * Scheduled job service implementation
 */
@Service
public class SysJobServiceImpl implements ISysJobService {

    private static final Logger log = LoggerFactory.getLogger(SysJobServiceImpl.class);

    @Autowired
    private SysJobRepository jobRepository;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobWebSocketService webSocketService;
    
    @Autowired
    private SysJobLogRepository jobLogRepository;

    @Override
    public List<SysJob> selectJobList(SysJob job) {
        return jobRepository.findAllActive();
    }

    @Override
    public SysJob selectJobById(Long jobId) {
        return jobRepository.findById(jobId).orElse(null);
    }

    @Override
    public void pauseJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        String oldStatus = job.getStatus();
        job.setStatus("1");
        jobRepository.save(job);
        scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        webSocketService.sendJobStatusChanged(jobId, job.getJobName(), oldStatus, "1");
    }

    @Override
    public void resumeJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        String oldStatus = job.getStatus();
        job.setStatus("0");
        jobRepository.save(job);
        scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        webSocketService.sendJobStatusChanged(jobId, job.getJobName(), oldStatus, "0");
    }

    @Override
    public void deleteJobByIds(Long[] jobIds) throws SchedulerException {
        for (Long jobId : jobIds) {
            SysJob job = jobRepository.findById(jobId).orElse(null);
            if (job != null) {
                scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, job.getJobGroup()));
                jobRepository.deleteById(jobId);
                webSocketService.sendJobDeleted(jobId, job.getJobName());
            }
        }
    }

    @Override
    public void deleteJob(SysJob job) throws SchedulerException {
        deleteJobByIds(new Long[]{job.getJobId()});
    }

    @Override
    public int changeStatus(SysJob job) throws SchedulerException {
        String status = job.getStatus();
        SysJob sysJob = jobRepository.findById(job.getJobId()).orElse(null);
        if (sysJob == null) {
            return 0;
        }
        if ("1".equals(status)) {
            pauseJob(sysJob);
        } else if ("0".equals(status)) {
            resumeJob(sysJob);
        }
        return 1;
    }

    @Override
    public Long run(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        SysJob sysJob = jobRepository.findById(jobId).orElse(null);
        if (sysJob == null) {
            throw new RuntimeException("Job not found");
        }
        String jobGroup = sysJob.getJobGroup();

        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        
        // Ensure the job exists in the scheduler before triggering
        if (!scheduler.checkExists(jobKey)) {
            log.info("Job {} not found in scheduler, creating it now", sysJob.getJobName());
            ScheduleUtils.createScheduleJob(scheduler, sysJob);
        }

        // Create a job log entry first to get an ID
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobId(jobId);
        jobLog.setJobName(sysJob.getJobName());
        jobLog.setJobGroup(jobGroup);
        jobLog.setInvokeTarget(sysJob.getInvokeTarget());
        jobLog.setStartTime(LocalDateTime.now());
        jobLog.setRetryCount(0);
        jobLog.setStatus("2"); // 2 = Running/Pending

        // Save log to generate ID
        SysJobLog savedLog = jobLogRepository.save(jobLog);
        Long jobLogId = savedLog.getJobLogId();

        // Add log ID to JobDataMap so the job execution can update it
        JobDataMap data = new JobDataMap();
        data.put("JOB_LOG_ID", jobLogId);

        // Trigger the job with the map
        scheduler.triggerJob(jobKey, data);
        webSocketService.sendJobStarted(jobId, sysJob.getJobName());

        return jobLogId;
    }

    @Override
    public void insertJob(SysJob job) throws SchedulerException {
        job.setCreateTime(java.time.LocalDateTime.now());
        job.setCreateBy("admin");
        validateNoCircularDependency(job.getJobId(), job.getDependentJobIds());
        jobRepository.save(job);

        // Check if cron expression is valid
        if (CronUtils.isValid(job.getCronExpression())) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }

        webSocketService.sendJobCreated(job.getJobId(), job.getJobName());
    }

    @Override
    public void updateJob(SysJob job) throws SchedulerException {
        SysJob existingJob = jobRepository.findById(job.getJobId()).orElse(null);
        if (existingJob == null) {
            return;
        }

        job.setUpdateTime(java.time.LocalDateTime.now());
        job.setUpdateBy("admin");
        validateNoCircularDependency(job.getJobId(), job.getDependentJobIds());

        JobKey jobKey = ScheduleUtils.getJobKey(existingJob.getJobId(), existingJob.getJobGroup());
        
        // Delete old scheduler job if it exists
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        jobRepository.save(job);

        // Create new scheduler job
        if (CronUtils.isValid(job.getCronExpression())) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
    }

    @Override
    public boolean checkCronExpressionIsValid(SysJob job) {
        return CronUtils.isValid(job.getCronExpression());
    }

    /**
     * Validate that dependent job IDs don't create a circular dependency chain.
     */
    private void validateNoCircularDependency(Long jobId, String dependentJobIds) {
        if (dependentJobIds == null || dependentJobIds.trim().isEmpty()) {
            return;
        }

        String[] ids = dependentJobIds.split(",");
        for (String idStr : ids) {
            Long depId;
            try {
                depId = Long.valueOf(idStr.trim());
            } catch (NumberFormatException e) {
                continue;
            }
            if (depId.equals(jobId)) {
                throw new RuntimeException("Circular dependency detected: Job " + jobId + " cannot depend on itself");
            }
            // Check if the dependent job depends on this job (direct circular)
            SysJob depJob = jobRepository.findById(depId).orElse(null);
            if (depJob != null && depJob.getDependentJobIds() != null) {
                String[] depDepIds = depJob.getDependentJobIds().split(",");
                for (String depDepIdStr : depDepIds) {
                    try {
                        if (Long.valueOf(depDepIdStr.trim()).equals(jobId)) {
                            throw new RuntimeException("Circular dependency detected: Job " + jobId + " and Job " + depId + " depend on each other");
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    /**
     * Get the full dependency chain for a job (recursive)
     */
    public List<SysJob> getJobDependencyChain(Long jobId) {
        List<SysJob> chain = new java.util.ArrayList<>();
        collectDependencies(jobId, chain, new java.util.HashSet<>());
        return chain;
    }

    private void collectDependencies(Long jobId, List<SysJob> chain, java.util.Set<Long> visited) {
        if (visited.contains(jobId)) return;
        visited.add(jobId);

        SysJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        chain.add(job);

        if (job.getDependentJobIds() != null && !job.getDependentJobIds().isEmpty()) {
            for (String idStr : job.getDependentJobIds().split(",")) {
                try {
                    Long depId = Long.valueOf(idStr.trim());
                    collectDependencies(depId, chain, visited);
                } catch (NumberFormatException ignored) {}
            }
        }
    }
}
