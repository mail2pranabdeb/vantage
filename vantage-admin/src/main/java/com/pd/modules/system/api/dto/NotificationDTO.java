package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long notificationId;
    private Long userId;
    private String title;
    private String content;
    private String notificationType;
    private String channel;
    private String status = "0";
    private String linkUrl;
    private LocalDateTime createTime;
    private LocalDateTime readTime;
    private LocalDateTime expiryTime;
}
