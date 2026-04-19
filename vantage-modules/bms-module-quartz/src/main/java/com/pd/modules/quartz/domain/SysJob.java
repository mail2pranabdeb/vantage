package com.pd.modules.quartz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Scheduled job entity - sys_job
 */
@Entity
@Table(name = "sys_job")
@Data
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

    /** Job Type (BEAN or REPORT) */
    @Column(name = "job_type", length = 20)
    private String jobType = "BEAN";

    /** Linked Report ID (for REPORT type jobs) */
    @Column(name = "report_id")
    private Long reportId;

    /** Report Email Group Key (from Dictionary) */
    @Column(name = "report_email_group", length = 500)
    private String reportEmailGroup;

    /** Invoke target string */
    @Column(name = "invoke_target", length = 500)
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

    /** Token for external webhook triggering */
    @Column(name = "webhook_token", length = 64)
    private String webhookToken;
}
