package com.pd.modules.quartz.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobDTO {
    private Long jobId;
    private String jobName;
    private String jobGroup;
    private String jobType = "BEAN";
    private Long reportId;
    private String reportParams;
    private String emailTemplateParams;
    private String reportEmailGroup;
    private String invokeTarget;
    private String cronExpression;
    private String misfirePolicy = "3";
    private String concurrent = "1";
    private String status = "0";
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
    private Integer maxRetryCount = 0;
    private Integer retryInterval = 60;
    private Integer timeoutSeconds = 3600;
    private Boolean notifyOnFailure = false;
    private String notificationEmails;
    private Long emailTemplateId;
    private String webhookUrl;
    private String dependentJobIds;
    private String timeZone = "UTC";
    private Boolean allowHoliday = true;
    private String templateName;
    private String webhookToken;
}
