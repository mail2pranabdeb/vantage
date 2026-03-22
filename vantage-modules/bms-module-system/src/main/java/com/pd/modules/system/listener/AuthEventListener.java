package com.pd.modules.system.listener;

import com.pd.common.event.auth.LoginFailureEvent;
import com.pd.common.event.auth.LoginSuccessEvent;
import com.pd.modules.system.domain.SysLogininfor;
import com.pd.modules.system.infrastructure.repository.SysLogininforRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Listener for authentication events.
 * Records login success/failure in sys_logininfor table.
 */
@Component
public class AuthEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthEventListener.class);

    private final SysLogininforRepository logininforRepository;

    public AuthEventListener(SysLogininforRepository logininforRepository) {
        this.logininforRepository = logininforRepository;
    }

    /**
     * Handle login success event asynchronously.
     */
    @EventListener
    @Async
    public void handleLoginSuccess(LoginSuccessEvent event) {
        try {
            // Generate trace-like IDs for logging correlation
            String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            
            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);
            MDC.put("event", "LOGIN_SUCCESS");
            
            log.info("=== LOGIN SUCCESS EVENT RECEIVED ===");
            log.info("Login success: user={}, ip={}, location={}", 
                event.getLoginName(), event.getIpAddress(), event.getLocation());

            SysLogininfor logininfor = new SysLogininfor();
            logininfor.setLoginName(event.getLoginName());
            logininfor.setStatus("0");
            logininfor.setIpaddr(event.getIpAddress());
            logininfor.setLoginLocation(event.getLocation());
            logininfor.setBrowser(event.getBrowser());
            logininfor.setOs(event.getOs());
            logininfor.setMsg("Login success");
            logininfor.setLoginTime(LocalDateTime.now());

            logininforRepository.save(logininfor);
            log.info("=== Inserted login record into sys_logininfor ===");
            
        } catch (Exception e) {
            log.error("Failed to process login success event", e);
        } finally {
            MDC.remove("event");
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }

    /**
     * Handle login failure event asynchronously.
     */
    @EventListener
    @Async
    public void handleLoginFailure(LoginFailureEvent event) {
        try {
            // Generate trace-like IDs for logging correlation
            String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            
            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);
            MDC.put("event", "LOGIN_FAILURE");
            
            log.info("=== LOGIN FAILURE EVENT RECEIVED ===");
            log.warn("Login failure: user={}, ip={}, location={}, msg={}", 
                event.getLoginName(), event.getIpAddress(), event.getLocation(), event.getMessage());

            SysLogininfor logininfor = new SysLogininfor();
            logininfor.setLoginName(event.getLoginName());
            logininfor.setStatus("1");
            logininfor.setIpaddr(event.getIpAddress());
            logininfor.setLoginLocation(event.getLocation());
            logininfor.setBrowser(event.getBrowser());
            logininfor.setOs(event.getOs());
            logininfor.setMsg(event.getMessage());
            logininfor.setLoginTime(LocalDateTime.now());

            logininforRepository.save(logininfor);
            log.info("=== Inserted login failure record into sys_logininfor ===");
            
        } catch (Exception e) {
            log.error("Failed to process login failure event", e);
        } finally {
            MDC.remove("event");
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }
}
