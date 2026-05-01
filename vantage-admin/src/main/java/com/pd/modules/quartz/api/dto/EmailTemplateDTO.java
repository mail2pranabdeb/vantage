package com.pd.modules.quartz.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmailTemplateDTO {
    private Long templateId;
    private String templateName;
    private String templateType;
    private String emailSubject;
    private String emailBody;
    private Boolean isDefault = false;
    private Boolean isActive = true;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;
    private String datasourceKey;
    private String querySql;
    private Boolean includeDataTable = false;
    private String dataTables;
    private String previewParams;
    private String runtimeParams;
}
