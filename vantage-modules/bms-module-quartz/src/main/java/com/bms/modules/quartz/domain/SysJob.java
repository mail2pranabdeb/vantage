package com.pd.modules.quartz.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Scheduled job entity - sys_job
 */
public class SysJob implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Task ID */
    private Long jobId;

    /** Task name */
    private String jobName;

    /** Task group name */
    private String jobGroup;

    /** Invoke target string */
    private String invokeTarget;

    /** Cron expression */
    private String cronExpression;

    /** Misfire policy */
    private String misfirePolicy = "3";

    /** Concurrent execution (0 allow, 1 deny) */
    private String concurrent;

    /** Task status (0 normal, 1 paused) */
    private String status;

    /** Create by */
    private String createBy;

    /** Create time */
    private LocalDateTime createTime;

    /** Update by */
    private String updateBy;

    /** Update time */
    private LocalDateTime updateTime;

    /** Remark */
    private String remark;

    // Getters and Setters

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

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getMisfirePolicy() {
        return misfirePolicy;
    }

    public void setMisfirePolicy(String misfirePolicy) {
        this.misfirePolicy = misfirePolicy;
    }

    public String getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(String concurrent) {
        this.concurrent = concurrent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
