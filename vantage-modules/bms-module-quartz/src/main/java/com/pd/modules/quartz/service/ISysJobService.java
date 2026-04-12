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
    void pauseJob(SysJob job) throws SchedulerException;

    /**
     * Resume job
     */
    void resumeJob(SysJob job) throws SchedulerException;

    /**
     * Delete job
     */
    void deleteJob(SysJob job) throws SchedulerException;

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
     * @return Job Log ID
     */
    Long run(SysJob job) throws SchedulerException;

    /**
     * Insert job
     */
    void insertJob(SysJob job) throws SchedulerException;

    /**
     * Update job
     */
    void updateJob(SysJob job) throws SchedulerException;

    /**
     * Validate cron expression
     */
    boolean checkCronExpressionIsValid(SysJob job);
}
