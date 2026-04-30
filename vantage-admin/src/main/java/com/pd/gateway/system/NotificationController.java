package com.pd.gateway.system;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysNotification;
import com.pd.modules.system.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system/notifications")
public class NotificationController extends BaseController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Get current user ID
     */
    private Long getCurrentUserId() {
        // Implementation to get current user ID from security context
        return 1L; // Note: Implement proper user ID extraction
    }

    /**
     * Get notifications for current user
     */
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        Long userId = getCurrentUserId();
        Page<SysNotification> page = notificationService.getNotifications(userId, pageNum, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("unreadCount", notificationService.getUnreadCount(userId));
        
        return success(result);
    }

    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    public AjaxResult unread(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        Long userId = getCurrentUserId();
        Page<SysNotification> page = notificationService.getUnreadNotifications(userId, pageNum, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        
        return success(result);
    }

    /**
     * Get unread count
     */
    @GetMapping("/unread-count")
    public AjaxResult unreadCount() {
        Long userId = getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return success(Map.of("count", count));
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    public AjaxResult markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return success();
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public AjaxResult markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return success();
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/statistics")
    public AjaxResult statistics() {
        Long userId = getCurrentUserId();
        Map<String, Object> stats = notificationService.getStatistics(userId);
        return success(stats);
    }

    /**
     * Send test notification
     */
    @PostMapping("/test")
    public AjaxResult sendTest(@RequestBody Map<String, String> params) {
        Long userId = getCurrentUserId();
        String title = params.get("title");
        String content = params.get("content");
        String type = params.getOrDefault("type", "INFO");
        
        notificationService.sendInAppNotification(userId, title, content, type);
        return success("Notification sent");
    }
}
