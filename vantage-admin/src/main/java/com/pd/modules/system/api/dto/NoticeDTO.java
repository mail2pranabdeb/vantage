package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeDTO {
    private Long noticeId;
    private String noticeTitle;
    private String noticeType;
    private String noticeContent;
    private String status = "0";
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
}
