package com.pd.modules.report.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Report definition entity
 */
@Entity
@Table(name = "sys_report")
public class SysReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "sys_report_seq", sequenceName = "sys_report_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_report_seq")
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

    // Getters and Setters
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    public String getReportKey() { return reportKey; }
    public void setReportKey(String reportKey) { this.reportKey = reportKey; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getDatasourceKey() { return datasourceKey; }
    public void setDatasourceKey(String datasourceKey) { this.datasourceKey = datasourceKey; }
    public String getSqlContent() { return sqlContent; }
    public void setSqlContent(String sqlContent) { this.sqlContent = sqlContent; }
    public String getParamsConfig() { return paramsConfig; }
    public void setParamsConfig(String paramsConfig) { this.paramsConfig = paramsConfig; }
    public String getColumnsConfig() { return columnsConfig; }
    public void setColumnsConfig(String columnsConfig) { this.columnsConfig = columnsConfig; }
    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
