package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysOperLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysOperLogRepository extends JpaRepository<SysOperLog, Long> {

    @Query("SELECT o FROM SysOperLog o WHERE 1=1 " +
           "AND (:title IS NULL OR o.title LIKE %:title%) " +
           "AND (:operName IS NULL OR o.operName LIKE %:operName%) " +
           "AND (:businessType IS NULL OR o.businessType = :businessType) " +
           "AND (:status IS NULL OR o.status = :status)")
    List<SysOperLog> findByCondition(@Param("title") String title,
                                     @Param("operName") String operName,
                                     @Param("businessType") Integer businessType,
                                     @Param("status") Integer status);

    @Query("SELECT o FROM SysOperLog o WHERE 1=1 " +
           "AND (:title IS NULL OR o.title LIKE %:title%) " +
           "AND (:operName IS NULL OR o.operName LIKE %:operName%) " +
           "AND (:businessType IS NULL OR o.businessType = :businessType) " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<SysOperLog> findByConditionPaginated(@Param("title") String title,
                                               @Param("operName") String operName,
                                               @Param("businessType") Integer businessType,
                                               @Param("status") Integer status,
                                               Pageable pageable);

    @Query("SELECT o FROM SysOperLog o ORDER BY o.operTime DESC")
    List<SysOperLog> findTopOperLogsByOrderByOperTimeDesc(Pageable pageable);

    @Query("SELECT o FROM SysOperLog o WHERE o.operName = :operName ORDER BY o.operTime DESC")
    List<SysOperLog> findByOperName(@Param("operName") String operName);

    @Query("SELECT o FROM SysOperLog o WHERE o.title = :title ORDER BY o.operTime DESC")
    List<SysOperLog> findByTitle(@Param("title") String operName);

    @Query("SELECT COUNT(o) FROM SysOperLog o WHERE o.status = :status")
    long countByStatus(@Param("status") String status);
}
