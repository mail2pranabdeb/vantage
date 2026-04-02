package com.pd.modules.system.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit Log Entity - Tracks all CRUD operations
 */
@Entity
@Table(name = "sys_audit_log")
public class SysAuditLog {

    @Id
    @SequenceGenerator(name = "sys_audit_log_seq", sequenceName = "sys_audit_log_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_audit_log_seq")
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "table_name", length = 100, nullable = false)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "operation_type", length = 20, nullable = false)
    private String operationType; // INSERT, UPDATE, DELETE

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues; // JSON format

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues; // JSON format

    @Column(name = "changed_fields", length = 1000)
    private String changedFields; // Comma-separated field names

    @Column(name = "operator", length = 100)
    private String operator;

    @Column(name = "operator_ip", length = 50)
    private String operatorIp;

    @Column(name = "operation_time")
    private LocalDateTime operationTime;

    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "remarks", length = 500)
    private String remarks;

    // Getters and Setters
    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getOldValues() { return oldValues; }
    public void setOldValues(String oldValues) { this.oldValues = oldValues; }
    public String getNewValues() { return newValues; }
    public void setNewValues(String newValues) { this.newValues = newValues; }
    public String getChangedFields() { return changedFields; }
    public void setChangedFields(String changedFields) { this.changedFields = changedFields; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getOperatorIp() { return operatorIp; }
    public void setOperatorIp(String operatorIp) { this.operatorIp = operatorIp; }
    public LocalDateTime getOperationTime() { return operationTime; }
    public void setOperationTime(LocalDateTime operationTime) { this.operationTime = operationTime; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
