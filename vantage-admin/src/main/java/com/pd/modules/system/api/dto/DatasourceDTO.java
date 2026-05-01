package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DatasourceDTO {
    private Long datasourceId;
    private String datasourceName;
    private String datasourceKey;
    private String dbType;
    private String url;
    private String username;
    private String password;
    private String driverClass;
    private String status = "0";
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
    private LocalDateTime lastTestTime;
    private String lastTestStatus = "0";
}
