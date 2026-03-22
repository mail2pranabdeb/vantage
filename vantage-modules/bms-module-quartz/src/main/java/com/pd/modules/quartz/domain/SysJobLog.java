package com.pd.modules.quartz.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Scheduled job log entity - sys_job_log
 */
public class SysJobLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Log ID */
    private Long jobLogId;

    /** Task name */
    private String jobName;

    /** Task group name */
    private String jobGroup;

    /** Invoke target string */
    private String invokeTarget;

    /** Log message */
    private String jobMessage;

    /** Execution status (0 normal, 1 failed) */
    private String status;

    /** Exception info */
    private String exceptionInfo;

    /** Start time */
    private LocalDateTime startTime;

    /** End time */
    private LocalDateTime endTime;

    // Getters and Setters

    public Long getJobLogId() {
        return jobLogId;
    }

    public void setJobLogId(Long jobLogId) {
        this.jobLogId = jobLogId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getInvokeTarget() {
        return invokeTarget;
    }

    public void setInvokeTarget(String invokeTarget) {
        this.invokeTarget = invokeTarget;
    }

    public String getJobMessage() {
        return jobMessage;
    }

    public void setJobMessage(String jobMessage) {
        this.jobMessage = jobMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
