package com.pd.modules.quartz.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Email template entity for job notifications
 */
@Entity
@Table(name = "sys_job_email_template")
@Data
public class EmailTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    /** Template name */
    @Column(name = "template_name", length = 64, nullable = false, unique = true)
    private String templateName;

    /** Template type (JOB_FAILURE, JOB_SUCCESS, JOB_COMPLETED) */
    @Column(name = "template_type", length = 32, nullable = false)
    private String templateType;

    /** Email subject */
    @Column(name = "email_subject", length = 255, nullable = false)
    private String emailSubject;

    /** Email body (supports HTML) */
    @Column(name = "email_body", columnDefinition = "TEXT", nullable = false)
    private String emailBody;

    /** Is default template */
    @Column(name = "is_default")
    private Boolean isDefault = false;

    /** Is active */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /** Create time */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** Update time */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /** Remark */
    @Column(name = "remark", length = 500)
    private String remark;

    /** Datasource key for data table queries */
    @Column(name = "datasource_key", length = 50)
    private String datasourceKey;

    /** Custom SQL query to fetch data for HTML table */
    @Column(name = "query_sql", columnDefinition = "TEXT")
    private String querySql;

    /** Include data table in email */
    @Column(name = "include_data_table")
    private Boolean includeDataTable = false;

    /** Multiple data tables as JSON array */
    @Column(name = "data_tables", columnDefinition = "TEXT")
    private String dataTables;

    /** Preview parameters JSON for data table queries (preview only) */
    @Column(name = "preview_params", columnDefinition = "TEXT")
    private String previewParams;

    /** Runtime parameters JSON for job execution */
    @Column(name = "runtime_params", columnDefinition = "TEXT")
    private String runtimeParams;

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

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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

    public String getDatasourceKey() {
        return datasourceKey;
    }

    public void setDatasourceKey(String datasourceKey) {
        this.datasourceKey = datasourceKey;
    }

    public String getQuerySql() {
        return querySql;
    }

    public void setQuerySql(String querySql) {
        this.querySql = querySql;
    }

    public Boolean getIncludeDataTable() {
        return includeDataTable;
    }

    public void setIncludeDataTable(Boolean includeDataTable) {
        this.includeDataTable = includeDataTable;
    }

    public String getDataTables() {
        return dataTables;
    }

    public void setDataTables(String dataTables) {
        this.dataTables = dataTables;
    }

    public String getPreviewParams() {
        return previewParams;
    }

    public void setPreviewParams(String previewParams) {
        this.previewParams = previewParams;
    }

    public String getRuntimeParams() {
        return runtimeParams;
    }

    public void setRuntimeParams(String runtimeParams) {
        this.runtimeParams = runtimeParams;
    }
}
