package com.pd.modules.system.listener;

import com.pd.common.event.auth.LoginFailureEvent;
import com.pd.common.event.auth.LoginSuccessEvent;
import com.pd.modules.system.domain.SysLogininfor;
import com.pd.modules.system.infrastructure.repository.SysLogininforRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

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
     * Handle login success event after transaction commits.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLoginSuccess(LoginSuccessEvent event) {
        log.info("Login success: user={}, ip={}, location={}", 
            event.getLoginName(), event.getIpAddress(), event.getLocation());

        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setLoginName(event.getLoginName());
        logininfor.setStatus("0"); // 0 = success
        logininfor.setIpaddr(event.getIpAddress());
        logininfor.setLoginLocation(event.getLocation());
        logininfor.setBrowser(event.getBrowser());
        logininfor.setOs(event.getOs());
        logininfor.setMsg("Login success");
        logininfor.setLoginTime(LocalDateTime.now());

        logininforRepository.insert(logininfor);
    }

    /**
     * Handle login failure event after transaction commits.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLoginFailure(LoginFailureEvent event) {
        log.warn("Login failure: user={}, ip={}, location={}, msg={}", 
            event.getLoginName(), event.getIpAddress(), event.getLocation(), event.getMessage());

        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setLoginName(event.getLoginName());
        logininfor.setStatus("1"); // 1 = failure
        logininfor.setIpaddr(event.getIpAddress());
        logininfor.setLoginLocation(event.getLocation());
        logininfor.setBrowser(event.getBrowser());
        logininfor.setOs(event.getOs());
        logininfor.setMsg(event.getMessage());
        logininfor.setLoginTime(LocalDateTime.now());

        logininforRepository.insert(logininfor);
    }
}
