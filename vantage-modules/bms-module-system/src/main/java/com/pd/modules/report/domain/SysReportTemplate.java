package com.pd.modules.report.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import java.time.LocalDateTime;

/**
 * Report template entity for storing visual report designer configurations.
 * Supports SQL, Visual Builder, and Hybrid report modes.
 */
@Entity
@Table(name = "sys_report_template")
@Data
public class SysReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_name", length = 200, nullable = false)
    private String templateName;

    @Column(name = "template_key", length = 100, nullable = false, unique = true)
    private String templateKey;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "datasource_key", length = 100, nullable = false)
    private String datasourceKey;

    /**
     * Report mode: SQL, VISUAL_BUILDER, HYBRID
     */
    @Column(name = "report_mode", length = 20)
    private String reportMode = "SQL";

    @Lob
    @Column(name = "sql_content")
    private String sqlContent;

    /**
     * JSON: selected tables and join configurations
     */
    @Lob
    @Column(name = "tables_config")
    private String tablesConfig;

    /**
     * JSON: dragged columns with formatting, labels, widths
     */
    @Lob
    @Column(name = "columns_config")
    private String columnsConfig;

    /**
     * JSON: where clauses and filter configurations
     */
    @Lob
    @Column(name = "filters_config")
    private String filtersConfig;

    /**
     * JSON: grouping column configurations
     */
    @Lob
    @Column(name = "group_by_config")
    private String groupByConfig;

    /**
     * JSON: sorting configurations
     */
    @Lob
    @Column(name = "order_by_config")
    private String orderByConfig;

    /**
     * JSON: chart definitions (bar, line, pie, etc.)
     */
    @Lob
    @Column(name = "charts_config")
    private String chartsConfig;

    /**
     * JSON: page layout, header, footer settings
     */
    @Lob
    @Column(name = "layout_config")
    private String layoutConfig;

    /**
     * Output format: EXCEL, PDF, CSV, HTML, JSON
     */
    @Column(name = "output_format", length = 20)
    private String outputFormat = "EXCEL";

    @Column(name = "status", length = 1)
    private String status = "0"; // 0=Active, 1=Inactive, 2=Archived

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "parent_template_id")
    private Long parentTemplateId;

    @Column(name = "change_log", length = 500)
    private String changeLog;

    @Column(name = "create_by", length = 64, updatable = false)
    @CreatedBy
    private String createBy;

    @Column(name = "create_time", updatable = false)
    @CreationTimestamp
    private LocalDateTime createTime;

    @Column(name = "update_by", length = 64)
    @LastModifiedBy
    private String updateBy;

    @Column(name = "update_time")
    @UpdateTimestamp
    private LocalDateTime updateTime;

    @Column(name = "remark", length = 500)
    private String remark;

    }
