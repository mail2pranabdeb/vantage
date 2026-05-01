package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.NotificationDTO;
import java.util.Map;
import org.springframework.data.domain.Page;

/**
 * System module public API for notification operations.
 */
public interface SystemNotificationService {

    Page<NotificationDTO> getNotifications(Long userId, int pageNum, int pageSize);

    Page<NotificationDTO> getUnreadNotifications(Long userId, int pageNum, int pageSize);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    Map<String, Object> getStatistics(Long userId);

    void sendInAppNotification(Long userId, String title, String content, String type);
}
