package com.pd.modules.quartz.service.impl;

import com.pd.modules.quartz.api.QuartzJobService;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import com.pd.modules.quartz.util.CronUtils;
import com.pd.modules.quartz.util.ScheduleUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of QuartzJobService API.
 * This is the public implementation that external modules should use.
 */
@Service
public class QuartzJobServiceImpl implements QuartzJobService {

    private final SysJobRepository jobRepository;
    private final Scheduler scheduler;

    public QuartzJobServiceImpl(SysJobRepository jobRepository, Scheduler scheduler) {
        this.jobRepository = jobRepository;
        this.scheduler = scheduler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysJob> findAll() {
        return jobRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SysJob> findById(Long jobId) {
        return jobRepository.findById(jobId);
    }

    @Override
    @Transactional
    public SysJob createJob(SysJob job) {
        job.setCreateTime(LocalDateTime.now());
        job.setCreateBy("admin");
        job.setStatus("0"); // Default to active
        jobRepository.save(job);

        // Schedule the job if cron expression is valid
        if (CronUtils.isValid(job.getCronExpression())) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
        return job;
    }

    @Override
    @Transactional
    public SysJob updateJob(SysJob job) {
        Optional<SysJob> existingJobOpt = jobRepository.findById(job.getJobId());
        if (existingJobOpt.isEmpty()) {
            throw new RuntimeException("Job not found with id: " + job.getJobId());
        }

        SysJob existingJob = existingJobOpt.get();
        job.setUpdateTime(LocalDateTime.now());
        job.setUpdateBy("admin");

        try {
            // Delete old scheduler job
            scheduler.deleteJob(ScheduleUtils.getJobKey(existingJob.getJobId(), existingJob.getJobGroup()));

            jobRepository.save(job);

            // Create new scheduler job
            if (CronUtils.isValid(job.getCronExpression())) {
                ScheduleUtils.createScheduleJob(scheduler, job);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to update scheduled job", e);
        }
        return job;
    }

    @Override
    @Transactional
    public boolean deleteJob(Long jobId) {
        Optional<SysJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            SysJob job = jobOpt.get();
            try {
                scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, job.getJobGroup()));
                jobRepository.deleteById(jobId);
                return true;
            } catch (SchedulerException e) {
                throw new RuntimeException("Failed to delete scheduled job", e);
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void runJob(Long jobId) {
        Optional<SysJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            SysJob job = jobOpt.get();
            try {
                scheduler.triggerJob(ScheduleUtils.getJobKey(jobId, job.getJobGroup()));
            } catch (SchedulerException e) {
                throw new RuntimeException("Failed to run job", e);
            }
        }
    }

    @Override
    @Transactional
    public void pauseJob(Long jobId) {
        Optional<SysJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            SysJob job = jobOpt.get();
            job.setStatus("1");
            jobRepository.save(job);
            try {
                scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, job.getJobGroup()));
            } catch (SchedulerException e) {
                throw new RuntimeException("Failed to pause job", e);
            }
        }
    }

    @Override
    @Transactional
    public void resumeJob(Long jobId) {
        Optional<SysJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            SysJob job = jobOpt.get();
            job.setStatus("0");
            jobRepository.save(job);
            try {
                scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, job.getJobGroup()));
            } catch (SchedulerException e) {
                throw new RuntimeException("Failed to resume job", e);
            }
        }
    }
}
