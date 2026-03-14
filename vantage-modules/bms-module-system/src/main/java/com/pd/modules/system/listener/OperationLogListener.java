package com.pd.modules.system.listener;

import com.pd.common.event.operation.OperationLogEvent;
import com.pd.modules.system.domain.SysOperLog;
import com.pd.modules.system.infrastructure.repository.SysOperLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * Listener for operation log events.
 * Records user operations in sys_oper_log table.
 */
@Component
public class OperationLogListener {

    private static final Logger log = LoggerFactory.getLogger(OperationLogListener.class);

    private final SysOperLogRepository operLogRepository;

    public OperationLogListener(SysOperLogRepository operLogRepository) {
        this.operLogRepository = operLogRepository;
    }

    /**
     * Handle operation log event after transaction commits.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOperationLog(OperationLogEvent event) {
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

        operLogRepository.insert(operLog);
    }
}
