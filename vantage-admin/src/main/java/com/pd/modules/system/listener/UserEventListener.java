package com.pd.modules.system.listener;

import com.pd.common.event.user.UserCreatedEvent;
import com.pd.common.event.user.UserDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener for user events within the system module.
 * Can be extended to perform additional operations when user events occur.
 */
@Component
public class UserEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);

    /**
     * Handle user created event after transaction commits.
     * Other modules can use this pattern to react to user creation.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("User created event received: userId={}, loginName={}, userName={}", 
            event.getUserId(), event.getLoginName(), event.getUserName());
        
        // Example: Could trigger welcome email, create default settings, etc.
        // This is where cross-module communication happens without direct dependencies
    }

    /**
     * Handle user deleted event after transaction commits.
     * Other modules can use this pattern to cleanup related data.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("User deleted event received: userId={}, loginName={}", 
            event.getUserId(), event.getLoginName());
        
        // Example: Could cleanup related data in other modules
        // e.g., delete user preferences, notifications, etc.
    }
}
