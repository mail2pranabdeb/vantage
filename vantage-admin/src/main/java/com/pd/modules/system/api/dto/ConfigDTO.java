package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConfigDTO {
    private Long configId;
    private String configName;
    private String configKey;
    private String configValue;
    private String configType = "Y";
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
}
