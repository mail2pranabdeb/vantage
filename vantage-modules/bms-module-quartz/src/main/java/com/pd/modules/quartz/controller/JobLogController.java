package com.pd.modules.quartz.controller;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.quartz.domain.SysJobLog;
import com.pd.modules.quartz.infrastructure.repository.SysJobLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job execution log controller
 */
@RestController
@RequestMapping("/api/system/job-log")
public class JobLogController extends BaseController {

    @Autowired
    private SysJobLogRepository jobLogRepository;

    /**
     * Get all job logs
     */
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobGroup,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<SysJobLog> logs = jobLogRepository.findByCondition(jobName, jobGroup, status, startTime, endTime);
        return success(logs);
    }

    /**
     * Get job logs by job ID
     */
    @GetMapping("/job/{jobId}")
    public AjaxResult getByJobId(@PathVariable Long jobId) {
        List<SysJobLog> logs = jobLogRepository.findByJobId(jobId);
        return success(logs);
    }

    /**
     * Get job log by ID
     */
    @GetMapping("/{logId}")
    public AjaxResult getById(@PathVariable Long logId) {
        return jobLogRepository.findById(logId)
                .map(this::success)
                .orElse(error("Job log not found"));
    }

    /**
     * Get recent failed logs
     */
    @GetMapping("/failed/recent")
    public AjaxResult getRecentFailed(@RequestParam(defaultValue = "10") int limit) {
        List<SysJobLog> logs = jobLogRepository.findRecentFailed(limit);
        return success(logs);
    }

    /**
     * Get statistics for dashboard
     */
    @GetMapping("/statistics")
    public AjaxResult getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        SysJobLogRepository.JobLogStatistics stats = jobLogRepository.getStatistics(startDate, endDate);
        return success(stats);
    }

    /**
     * Delete job log by ID
     */
    @DeleteMapping("/{logId}")
    public AjaxResult remove(@PathVariable Long logId) {
        jobLogRepository.deleteById(logId);
        return success("Job log deleted successfully");
    }

    /**
     * Delete job logs by IDs
     */
    @DeleteMapping("/batch")
    public AjaxResult batchRemove(@RequestBody Long[] ids) {
        int count = jobLogRepository.deleteByIds(ids);
        return success("Deleted " + count + " job log(s)");
    }

    /**
     * Clean all job logs
     */
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        List<SysJobLog> logs = jobLogRepository.findAll();
        for (SysJobLog log : logs) {
            jobLogRepository.deleteById(log.getJobLogId());
        }
        return success("All job logs cleared");
    }
}
