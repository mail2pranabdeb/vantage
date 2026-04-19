package com.pd.modules.system.service;

import com.pd.modules.system.domain.SysNotification;
import com.pd.modules.system.infrastructure.repository.SysNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private SysNotificationRepository notificationRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * Send in-app notification
     */
    @Transactional
    public void sendInAppNotification(Long userId, String title, String content, String type) {
        sendInAppNotification(userId, title, content, type, null);
    }

    /**
     * Send in-app notification with link
     */
    @Transactional
    public void sendInAppNotification(Long userId, String title, String content, String type, String linkUrl) {
        SysNotification notification = new SysNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(type);
        notification.setChannel("IN_APP");
        notification.setStatus("0");
        notification.setLinkUrl(linkUrl);
        notification.setCreateTime(LocalDateTime.now());

        notificationRepository.save(notification);
        log.info("In-app notification sent to user {}: {}", userId, title);
    }

    /**
     * Send email notification asynchronously
     */
    @Async
    @Transactional
    public void sendEmailNotification(String to, String subject, String content) {
        if (mailSender == null) {
            log.warn("Mail sender not configured");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    /**
     * Send notification with user preferences
     */
    @Transactional
    public void sendNotification(Long userId, String title, String content, String type, 
                                  boolean sendEmail, boolean sendInApp) {
        if (sendInApp) {
            sendInAppNotification(userId, title, content, type);
        }

        if (sendEmail) {
            // Get user email from database (implementation needed)
            // sendEmailNotification(userEmail, title, content);
        }
    }

    /**
     * Get notifications for user
     */
    @Transactional(readOnly = true)
    public Page<SysNotification> getNotifications(Long userId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return notificationRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
    }

    /**
     * Get unread notifications
     */
    @Transactional(readOnly = true)
    public Page<SysNotification> getUnreadNotifications(Long userId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return notificationRepository.findByUserIdAndStatusOrderByCreateTimeDesc(userId, "0", pageable);
    }

    /**
     * Get unread count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, "0");
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /**
     * Get notification statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("unreadCount", getUnreadCount(userId));
        stats.put("totalCount", notificationRepository.count());
        return stats;
    }

    /**
     * Broadcast notification to all users
     */
    @Transactional
    public void broadcastNotification(String title, String content, String type) {
        // Implementation to send to all users
        log.info("Broadcast notification: {}", title);
    }
}
