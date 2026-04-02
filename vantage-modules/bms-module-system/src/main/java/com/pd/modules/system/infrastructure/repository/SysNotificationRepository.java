package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SysNotificationRepository extends JpaRepository<SysNotification, Long> {

    Page<SysNotification> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    Page<SysNotification> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, String status, Pageable pageable);

    long countByUserIdAndStatus(Long userId, String status);

    @Modifying
    @Query("UPDATE SysNotification n SET n.status = '1', n.readTime = CURRENT_TIMESTAMP WHERE n.userId = :userId")
    void markAllAsRead(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE SysNotification n SET n.status = '1', n.readTime = CURRENT_TIMESTAMP WHERE n.notificationId = :id")
    void markAsRead(@Param("id") Long id);
}
