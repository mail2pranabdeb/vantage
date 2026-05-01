package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogininforDTO {
    private Long infoId;
    private String loginName;
    private String status;
    private String ipaddr;
    private String loginLocation;
    private String browser;
    private String os;
    private String msg;
    private LocalDateTime loginTime;
}
