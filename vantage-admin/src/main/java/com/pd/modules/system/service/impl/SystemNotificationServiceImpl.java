package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemNotificationService;
import com.pd.modules.system.api.dto.NotificationDTO;
import com.pd.modules.system.domain.SysNotification;
import com.pd.modules.system.service.NotificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SystemNotificationServiceImpl implements SystemNotificationService {

    private final NotificationService notificationService;

    public SystemNotificationServiceImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public Page<NotificationDTO> getNotifications(Long userId, int pageNum, int pageSize) {
        return notificationService.getNotifications(userId, pageNum, pageSize).map(this::toDTO);
    }

    @Override
    public Page<NotificationDTO> getUnreadNotifications(Long userId, int pageNum, int pageSize) {
        return notificationService.getUnreadNotifications(userId, pageNum, pageSize).map(this::toDTO);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationService.getUnreadCount(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationService.markAsRead(notificationId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationService.markAllAsRead(userId);
    }

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        return notificationService.getStatistics(userId);
    }

    @Override
    public void sendInAppNotification(Long userId, String title, String content, String type) {
        notificationService.sendInAppNotification(userId, title, content, type);
    }

    private NotificationDTO toDTO(SysNotification entity) {
        NotificationDTO dto = new NotificationDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
