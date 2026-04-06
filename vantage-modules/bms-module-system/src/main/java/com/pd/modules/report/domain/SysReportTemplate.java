package com.pd.modules.report.domain;

import jakarta.persistence.*;
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
@SequenceGenerator(name = "report_template_seq", sequenceName = "sys_report_template_seq", allocationSize = 1)
public class SysReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_template_seq")
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

    // Getters and Setters

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDatasourceKey() {
        return datasourceKey;
    }

    public void setDatasourceKey(String datasourceKey) {
        this.datasourceKey = datasourceKey;
    }

    public String getReportMode() {
        return reportMode;
    }

    public void setReportMode(String reportMode) {
        this.reportMode = reportMode;
    }

    public String getSqlContent() {
        return sqlContent;
    }

    public void setSqlContent(String sqlContent) {
        this.sqlContent = sqlContent;
    }

    public String getTablesConfig() {
        return tablesConfig;
    }

    public void setTablesConfig(String tablesConfig) {
        this.tablesConfig = tablesConfig;
    }

    public String getColumnsConfig() {
        return columnsConfig;
    }

    public void setColumnsConfig(String columnsConfig) {
        this.columnsConfig = columnsConfig;
    }

    public String getFiltersConfig() {
        return filtersConfig;
    }

    public void setFiltersConfig(String filtersConfig) {
        this.filtersConfig = filtersConfig;
    }

    public String getGroupByConfig() {
        return groupByConfig;
    }

    public void setGroupByConfig(String groupByConfig) {
        this.groupByConfig = groupByConfig;
    }

    public String getOrderByConfig() {
        return orderByConfig;
    }

    public void setOrderByConfig(String orderByConfig) {
        this.orderByConfig = orderByConfig;
    }

    public String getChartsConfig() {
        return chartsConfig;
    }

    public void setChartsConfig(String chartsConfig) {
        this.chartsConfig = chartsConfig;
    }

    public String getLayoutConfig() {
        return layoutConfig;
    }

    public void setLayoutConfig(String layoutConfig) {
        this.layoutConfig = layoutConfig;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getParentTemplateId() {
        return parentTemplateId;
    }

    public void setParentTemplateId(Long parentTemplateId) {
        this.parentTemplateId = parentTemplateId;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }
}
