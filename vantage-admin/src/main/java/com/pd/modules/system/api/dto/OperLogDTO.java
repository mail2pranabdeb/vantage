package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperLogDTO {
    private Long operId;
    private String title;
    private Integer businessType;
    private String method;
    private String requestMethod;
    private Integer operatorType;
    private String operName;
    private String deptName;
    private String operUrl;
    private String operIp;
    private String operLocation;
    private String operParam;
    private String jsonResult;
    private Integer status;
    private String errorMsg;
    private LocalDateTime operTime;
    private Long costTime;
    private String oldValues;
    private String newValues;
    private String changedFields;
}
