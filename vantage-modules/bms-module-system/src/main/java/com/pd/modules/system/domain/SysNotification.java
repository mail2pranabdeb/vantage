package com.pd.modules.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Notification Entity
 */
@Entity
@Table(name = "sys_notification")
@Data
public class SysNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "notification_type", length = 50)
    private String notificationType;

    @Column(name = "channel", length = 50)
    private String channel;

    @Column(name = "status", length = 1)
    private String status = "0";

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "read_time")
    private LocalDateTime readTime;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;
}