package com.pd.modules.quartz.service;

import java.util.List;
import com.pd.modules.quartz.domain.SysJobLog;

/**
 * Scheduled job log service interface
 */
public interface ISysJobLogService {
    /**
     * Get list of job logs
     */
    List<SysJobLog> selectJobLogList(SysJobLog jobLog);

    /**
     * Get job log by ID
     */
    SysJobLog selectJobLogById(Long jobLogId);

    /**
     * Add job log
     */
    void addJobLog(SysJobLog jobLog);

    /**
     * Delete job logs by IDs
     */
    int deleteJobLogByIds(Long[] ids);

    /**
     * Delete job log by ID
     */
    int deleteJobLogById(Long jobId);

    /**
     * Clean all job logs
     */
    void cleanJobLog();
}
