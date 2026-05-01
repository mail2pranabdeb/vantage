package com.pd.modules.quartz.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobLogDTO {
    private Long jobLogId;
    private Long jobId;
    private String jobName;
    private String jobGroup;
    private String invokeTarget;
    private String jobMessage;
    private String status;
    private String exceptionInfo;
    private Long executionDuration;
    private Integer retryCount = 0;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
