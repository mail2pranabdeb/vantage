package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportTemplateDTO {
    private Long templateId;
    private String templateName;
    private String templateKey;
    private String description;
    private String datasourceKey;
    private String reportMode = "SQL";
    private String sqlContent;
    private String tablesConfig;
    private String columnsConfig;
    private String filtersConfig;
    private String groupByConfig;
    private String orderByConfig;
    private String chartsConfig;
    private String layoutConfig;
    private String outputFormat = "EXCEL";
    private String status = "0";
    private Integer version = 1;
    private Long parentTemplateId;
    private String changeLog;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
}
