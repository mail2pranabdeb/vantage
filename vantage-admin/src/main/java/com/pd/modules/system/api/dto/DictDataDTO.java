package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DictDataDTO {
    private Long dictCode;
    private Integer dictSort;
    private String dictLabel;
    private String dictValue;
    private String dictType;
    private String cssClass;
    private String listClass;
    private String isDefault;
    private String status;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
}
