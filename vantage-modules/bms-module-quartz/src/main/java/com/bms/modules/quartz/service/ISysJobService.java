package com.pd.modules.quartz.service;

import java.util.List;
import org.quartz.SchedulerException;
import com.pd.modules.quartz.domain.SysJob;

/**
 * Scheduled job service interface
 */
public interface ISysJobService {
    /**
     * Get list of scheduled jobs
     */
    List<SysJob> selectJobList(SysJob job);

    /**
     * Get job by ID
     */
    SysJob selectJobById(Long jobId);

    /**
     * Pause job
     */
    int pauseJob(SysJob job) throws SchedulerException;

    /**
     * Resume job
     */
    int resumeJob(SysJob job) throws SchedulerException;

    /**
     * Delete job
     */
    int deleteJob(SysJob job) throws SchedulerException;

    /**
     * Delete jobs by IDs
     */
    void deleteJobByIds(Long[] ids) throws SchedulerException;

    /**
     * Change job status
     */
    int changeStatus(SysJob job) throws SchedulerException;

    /**
     * Run job immediately
     */
    boolean run(SysJob job) throws SchedulerException;

    /**
     * Insert job
     */
    int insertJob(SysJob job) throws SchedulerException;

    /**
     * Update job
     */
    int updateJob(SysJob job) throws SchedulerException;

    /**
     * Validate cron expression
     */
    boolean checkCronExpressionIsValid(String cronExpression);
}
