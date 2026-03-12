package com.pd.modules.quartz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Scheduled job entity - sys_job
 */
@Entity
@Table(name = "sys_job")
public class SysJob implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Task ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    /** Task name */
    @Column(name = "job_name", length = 64)
    private String jobName;

    /** Task group name */
    @Column(name = "job_group", length = 64)
    private String jobGroup;

    /** Invoke target string */
    @Column(name = "invoke_target", length = 500, nullable = false)
    private String invokeTarget;

    /** Cron expression */
    @Column(name = "cron_expression", length = 255)
    private String cronExpression;

    /** Misfire policy */
    @Column(name = "misfire_policy", length = 20)
    private String misfirePolicy = "3";

    /** Concurrent execution (true allow, false deny) */
    @Column(name = "concurrent", length = 1)
    private String concurrent = "1";

    /** Task status (0 normal, 1 paused) */
    @Column(name = "status", length = 1)
    private String status = "0";

    /** Create by */
    @Column(name = "create_by", length = 64)
    private String createBy;

    /** Create time */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** Update by */
    @Column(name = "update_by", length = 64)
    private String updateBy;

    /** Update time */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /** Remark */
    @Column(name = "remark", length = 500)
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
