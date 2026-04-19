package com.pd.modules.quartz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Scheduled job log entity - sys_job_log
 */
@Entity
@Table(name = "sys_job_log")
@Data
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
}
