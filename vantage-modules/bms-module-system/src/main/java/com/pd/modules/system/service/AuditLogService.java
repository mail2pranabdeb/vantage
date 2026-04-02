package com.pd.modules.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.modules.system.domain.SysAuditLog;
import com.pd.modules.system.infrastructure.repository.SysAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private SysAuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Log audit trail
     */
    @Transactional
    public void logAudit(String tableName, Long recordId, String operationType,
                         Object oldValue, Object newValue, String module) {
        try {
            SysAuditLog audit = new SysAuditLog();
            audit.setTableName(tableName);
            audit.setRecordId(recordId);
            audit.setOperationType(operationType);
            audit.setModule(module);
            audit.setOperator(getCurrentUsername());
            audit.setOperatorIp(getClientIp());
            audit.setOperationTime(LocalDateTime.now());

            // Convert objects to JSON
            if (oldValue != null) {
                audit.setOldValues(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                audit.setNewValues(objectMapper.writeValueAsString(newValue));
                audit.setChangedFields(compareObjects(oldValue, newValue));
            }

            auditLogRepository.save(audit);
            log.info("Audit logged: {} {} by {}", operationType, tableName, audit.getOperator());
        } catch (Exception e) {
            log.error("Failed to log audit", e);
        }
    }

    /**
     * Compare two objects and return changed fields
     */
    private String compareObjects(Object oldObj, Object newObj) {
        if (oldObj == null || newObj == null) {
            return "";
        }

        try {
            Map<String, Object> oldMap = objectMapper.convertValue(oldObj, Map.class);
            Map<String, Object> newMap = objectMapper.convertValue(newObj, Map.class);

            List<String> changedFields = new ArrayList<>();
            for (String key : newMap.keySet()) {
                Object oldValue = oldMap.get(key);
                Object newValue = newMap.get(key);
                if (!Objects.equals(oldValue, newValue)) {
                    changedFields.add(key);
                }
            }

            return String.join(",", changedFields);
        } catch (Exception e) {
            log.error("Failed to compare objects", e);
            return "";
        }
    }

    /**
     * Get current username from security context
     */
    private String getCurrentUsername() {
        try {
            Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                return ((org.springframework.security.core.userdetails.User) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        } catch (Exception e) {
            // Ignore
        }
        return "system";
    }

    /**
     * Get client IP address
     */
    private String getClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
            
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Get audit logs with pagination
     */
    @Transactional(readOnly = true)
    public Page<SysAuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findRecent(pageable);
    }

    /**
     * Search audit logs
     */
    @Transactional(readOnly = true)
    public Page<SysAuditLog> searchAuditLogs(String tableName, String operator, 
                                              String module, LocalDateTime startTime, 
                                              LocalDateTime endTime, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(tableName, operator, module, startTime, endTime, pageable);
    }

    /**
     * Get audit history for specific record
     */
    @Transactional(readOnly = true)
    public List<SysAuditLog> getAuditHistory(String tableName, Long recordId) {
        return auditLogRepository.findByTableNameAndRecordIdOrderByOperationTimeDesc(tableName, recordId);
    }

    /**
     * Get audit statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAuditStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", auditLogRepository.count());
        stats.put("insertCount", auditLogRepository.countByOperationType("INSERT"));
        stats.put("updateCount", auditLogRepository.countByOperationType("UPDATE"));
        stats.put("deleteCount", auditLogRepository.countByOperationType("DELETE"));
        return stats;
    }
}
