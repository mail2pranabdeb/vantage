package com.pd.modules.report.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Report definition entity
 */
@Entity
@Table(name = "sys_report")
@Data
public class SysReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "report_name", length = 100, nullable = false)
    private String reportName;

    @Column(name = "report_key", length = 50, nullable = false, unique = true)
    private String reportKey;

    @Column(name = "report_type", length = 20)
    private String reportType = "SQL";

    @Column(name = "datasource_key", length = 50)
    private String datasourceKey = "master";

    @Column(name = "sql_content", columnDefinition = "TEXT", nullable = false)
    private String sqlContent;

    @Column(name = "params_config", length = 2000)
    private String paramsConfig;

    @Column(name = "columns_config", length = 4000)
    private String columnsConfig;

    @Column(name = "output_format", length = 20)
    private String outputFormat = "EXCEL";

    @Column(name = "status", length = 1)
    private String status = "0";

    @Column(name = "create_by", length = 64)
    private String createBy;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_by", length = 64)
    private String updateBy;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "schedule_enabled")
    private Boolean scheduleEnabled = false;

    @Column(name = "schedule_cron", length = 50)
    private String scheduleCron;

    @Column(name = "email_enabled")
    private Boolean emailEnabled = false;

    @Column(name = "email_recipients", length = 1000)
    private String emailRecipients;

    @Column(name = "email_subject", length = 255)
    private String emailSubject;

    @Column(name = "template_id")
    private Long templateId;

    @Transient
    private String templateName;

    }
