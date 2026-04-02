package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SysAuditLogRepository extends JpaRepository<SysAuditLog, Long> {

    /**
     * Find audit logs by table name
     */
    Page<SysAuditLog> findByTableName(String tableName, Pageable pageable);

    /**
     * Find audit logs by operator
     */
    Page<SysAuditLog> findByOperator(String operator, Pageable pageable);

    /**
     * Find audit logs by time range
     */
    @Query("SELECT a FROM SysAuditLog a WHERE a.operationTime BETWEEN :startTime AND :endTime ORDER BY a.operationTime DESC")
    Page<SysAuditLog> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime, 
                                       Pageable pageable);

    /**
     * Find audit logs by table and record ID
     */
    List<SysAuditLog> findByTableNameAndRecordIdOrderByOperationTimeDesc(String tableName, Long recordId);

    /**
     * Count audit logs by operation type
     */
    long countByOperationType(String operationType);

    /**
     * Count audit logs by operator
     */
    long countByOperator(String operator);

    /**
     * Find recent audit logs
     */
    @Query("SELECT a FROM SysAuditLog a ORDER BY a.operationTime DESC")
    Page<SysAuditLog> findRecent(Pageable pageable);

    /**
     * Search audit logs
     */
    @Query("SELECT a FROM SysAuditLog a WHERE " +
           "(:tableName IS NULL OR a.tableName LIKE %:tableName%) AND " +
           "(:operator IS NULL OR a.operator LIKE %:operator%) AND " +
           "(:module IS NULL OR a.module LIKE %:module%) AND " +
           "(:startTime IS NULL OR a.operationTime >= :startTime) AND " +
           "(:endTime IS NULL OR a.operationTime <= :endTime)")
    Page<SysAuditLog> searchAuditLogs(@Param("tableName") String tableName,
                                       @Param("operator") String operator,
                                       @Param("module") String module,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       Pageable pageable);
}
