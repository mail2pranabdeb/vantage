package com.pd.modules.quartz.api;

import com.pd.modules.quartz.domain.SysJob;
import java.util.List;
import java.util.Optional;

/**
 * Quartz module public API for scheduled job operations.
 * This interface defines the contract for external modules to interact with the quartz module.
 */
public interface QuartzJobService {

    /**
     * Get all jobs
     * @return list of all jobs
     */
    List<SysJob> findAll();

    /**
     * Get job by ID
     * @param jobId the job ID
     * @return optional containing the job if found
     */
    Optional<SysJob> findById(Long jobId);

    /**
     * Create a new scheduled job
     * @param job the job to create
     * @return the created job
     */
    SysJob createJob(SysJob job);

    /**
     * Update an existing job
     * @param job the job to update
     * @return the updated job
     */
    SysJob updateJob(SysJob job);

    /**
     * Delete a job
     * @param jobId the job ID to delete
     * @return true if deleted successfully
     */
    boolean deleteJob(Long jobId);

    /**
     * Run a job immediately
     * @param jobId the job ID to run
     */
    void runJob(Long jobId);

    /**
     * Pause a job
     * @param jobId the job ID to pause
     */
    void pauseJob(Long jobId);

    /**
     * Resume a paused job
     * @param jobId the job ID to resume
     */
    void resumeJob(Long jobId);
}
