package com.pd.modules.system.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Notification Entity
 */
@Entity
@Table(name = "sys_notification")
public class SysNotification {

    @Id
    @SequenceGenerator(name = "sys_notification_seq", sequenceName = "sys_notification_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_notification_seq")
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "notification_type", length = 50)
    private String notificationType; // INFO, WARNING, ERROR, SUCCESS

    @Column(name = "channel", length = 50)
    private String channel; // IN_APP, EMAIL, SMS, PUSH

    @Column(name = "status", length = 1)
    private String status = "0"; // 0=Unread, 1=Read

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "read_time")
    private LocalDateTime readTime;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    // Getters and Setters
    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getReadTime() { return readTime; }
    public void setReadTime(LocalDateTime readTime) { this.readTime = readTime; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
}
