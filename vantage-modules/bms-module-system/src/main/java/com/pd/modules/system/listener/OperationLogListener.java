package com.pd.modules.system.listener;

import com.pd.common.event.operation.OperationLogEvent;
import com.pd.modules.system.domain.SysOperLog;
import com.pd.modules.system.infrastructure.repository.SysOperLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Listener for operation log events.
 * Records user operations in sys_oper_log table.
 */
@Component
public class OperationLogListener {

    private static final Logger log = LoggerFactory.getLogger(OperationLogListener.class);
    private static int instanceCount = 0;
    private final int instanceId;

    private final SysOperLogRepository operLogRepository;

    public OperationLogListener(SysOperLogRepository operLogRepository) {
        this.operLogRepository = operLogRepository;
        this.instanceId = ++instanceCount;
        log.info("=== OperationLogListener instance {} created ===", instanceId);
    }

    /**
     * Handle operation log event asynchronously.
     */
    @EventListener
    @Async
    public void handleOperationLog(OperationLogEvent event) {
        log.info("=== handleOperationLog called [Instance {}] - Event ID: {} ===", instanceId, System.identityHashCode(event));
        try {
            // Generate trace-like IDs for logging correlation
            String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);
            MDC.put("event", "OPERATION_LOG");

            log.info("=== OPERATION LOG EVENT RECEIVED [Instance {}] ===", instanceId);
            log.info("Operation log: title={}, operName={}, url={}, status={}",
                event.getTitle(), event.getOperName(), event.getOperUrl(), event.getStatus());

            SysOperLog operLog = new SysOperLog();
            operLog.setTitle(event.getTitle());
            operLog.setBusinessType(event.getBusinessType());
            operLog.setMethod(event.getMethod());
            operLog.setRequestMethod(event.getRequestMethod());
            operLog.setOperatorType(event.getOperatorType());
            operLog.setOperName(event.getOperName());
            operLog.setDeptName(event.getDeptName());
            operLog.setOperUrl(event.getOperUrl());
            operLog.setOperIp(event.getOperIp());
            operLog.setOperLocation(event.getOperLocation());
            operLog.setOperParam(event.getOperParam());
            operLog.setJsonResult(event.getJsonResult());
            operLog.setStatus(event.getStatus());
            operLog.setErrorMsg(event.getErrorMsg());
            operLog.setOperTime(LocalDateTime.now());
            operLog.setCostTime(event.getCostTime());

            operLogRepository.save(operLog);
            log.info("=== Inserted operation log record into sys_oper_log ===");
            
        } catch (Exception e) {
            log.error("Failed to process operation log event", e);
        } finally {
            MDC.remove("event");
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }
}
