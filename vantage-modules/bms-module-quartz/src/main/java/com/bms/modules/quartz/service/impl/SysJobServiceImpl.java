package com.pd.modules.quartz.service.impl;

import java.util.List;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import com.pd.modules.quartz.service.ISysJobService;
import com.pd.modules.quartz.util.CronUtils;
import com.pd.modules.quartz.util.ScheduleUtils;

/**
 * Scheduled job service implementation
 */
@Service
public class SysJobServiceImpl implements ISysJobService {

    @Autowired
    private SysJobRepository jobRepository;

    @Autowired
    private Scheduler scheduler;

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
        job.setStatus("1");
        jobRepository.save(job);
        scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
    }

    @Override
    public void resumeJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus("0");
        jobRepository.save(job);
        scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
    }

    @Override
    public void deleteJobByIds(Long[] jobIds) throws SchedulerException {
        for (Long jobId : jobIds) {
            SysJob job = jobRepository.findById(jobId).orElse(null);
            if (job != null) {
                scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, job.getJobGroup()));
                jobRepository.deleteById(jobId);
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
    public void run(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        SysJob sysJob = jobRepository.findById(jobId).orElse(null);
        if (sysJob == null) {
            return;
        }
        String jobGroup = sysJob.getJobGroup();
        scheduler.triggerJob(ScheduleUtils.getJobKey(jobId, jobGroup));
    }

    @Override
    public void insertJob(SysJob job) throws SchedulerException {
        job.setCreateTime(java.time.LocalDateTime.now());
        job.setCreateBy("admin");
        jobRepository.save(job);
        
        // Check if cron expression is valid
        if (CronUtils.isValid(job.getCronExpression())) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
    }

    @Override
    public void updateJob(SysJob job) throws SchedulerException {
        SysJob existingJob = jobRepository.findById(job.getJobId()).orElse(null);
        if (existingJob == null) {
            return;
        }
        
        job.setUpdateTime(java.time.LocalDateTime.now());
        job.setUpdateBy("admin");
        
        // Delete old scheduler job
        scheduler.deleteJob(ScheduleUtils.getJobKey(existingJob.getJobId(), existingJob.getJobGroup()));
        
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
}
