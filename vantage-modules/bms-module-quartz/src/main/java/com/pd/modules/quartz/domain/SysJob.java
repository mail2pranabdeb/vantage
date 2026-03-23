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

    /** Maximum retry count for failed jobs */
    @Column(name = "max_retry_count")
    private Integer maxRetryCount = 0;

    /** Retry interval in seconds */
    @Column(name = "retry_interval")
    private Integer retryInterval = 60;

    /** Timeout in seconds */
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 3600;

    /** Enable email notification on failure */
    @Column(name = "notify_on_failure")
    private Boolean notifyOnFailure = false;

    /** Email addresses for notifications (comma-separated) */
    @Column(name = "notification_emails", length = 500)
    private String notificationEmails;

    /** Email template ID for notifications */
    @Column(name = "email_template_id")
    private Long emailTemplateId;

    /** Webhook URL for notifications */
    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    /** Dependent job IDs (comma-separated) */
    @Column(name = "dependent_job_ids", length = 500)
    private String dependentJobIds;

    /** Time zone for scheduling */
    @Column(name = "time_zone", length = 50)
    private String timeZone = "UTC";

    /** Allow execution on holidays */
    @Column(name = "allow_holiday")
    private Boolean allowHoliday = true;

    /** Job template name */
    @Column(name = "template_name", length = 64)
    private String templateName;

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

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Boolean getNotifyOnFailure() {
        return notifyOnFailure;
    }

    public void setNotifyOnFailure(Boolean notifyOnFailure) {
        this.notifyOnFailure = notifyOnFailure;
    }

    public String getNotificationEmails() {
        return notificationEmails;
    }

    public void setNotificationEmails(String notificationEmails) {
        this.notificationEmails = notificationEmails;
    }

    public Long getEmailTemplateId() {
        return emailTemplateId;
    }

    public void setEmailTemplateId(Long emailTemplateId) {
        this.emailTemplateId = emailTemplateId;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getDependentJobIds() {
        return dependentJobIds;
    }

    public void setDependentJobIds(String dependentJobIds) {
        this.dependentJobIds = dependentJobIds;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Boolean getAllowHoliday() {
        return allowHoliday;
    }

    public void setAllowHoliday(Boolean allowHoliday) {
        this.allowHoliday = allowHoliday;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
