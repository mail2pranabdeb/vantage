package com.pd.modules.quartz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Scheduled job log entity - sys_job_log
 */
@Entity
@Table(name = "sys_job_log")
public class SysJobLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Log ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_log_id")
    private Long jobLogId;

    /** Task ID */
    @Column(name = "job_id")
    private Long jobId;

    /** Task name */
    @Column(name = "job_name", length = 64)
    private String jobName;

    /** Task group name */
    @Column(name = "job_group", length = 64)
    private String jobGroup;

    /** Invoke target string */
    @Column(name = "invoke_target", length = 500)
    private String invokeTarget;

    /** Log message */
    @Column(name = "job_message", length = 2000)
    private String jobMessage;

    /** Execution status (0 normal, 1 failed) */
    @Column(name = "status", length = 1)
    private String status;

    /** Exception info */
    @Column(name = "exception_info", columnDefinition = "TEXT")
    private String exceptionInfo;

    /** Execution duration in milliseconds */
    @Column(name = "execution_duration")
    private Long executionDuration;

    /** Retry count */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /** Start time */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /** End time */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** Create time */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    // Getters and Setters

    public Long getJobLogId() {
        return jobLogId;
    }

    public void setJobLogId(Long jobLogId) {
        this.jobLogId = jobLogId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
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

    public Long getExecutionDuration() {
        return executionDuration;
    }

    public void setExecutionDuration(Long executionDuration) {
        this.executionDuration = executionDuration;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
