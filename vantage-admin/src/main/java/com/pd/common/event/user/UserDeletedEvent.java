package com.pd.common.event.user;

import com.pd.common.event.DomainEvent;

/**
 * Event published when a user is deleted.
 * Other modules can listen to this event for cleanup operations.
 */
public class UserDeletedEvent extends DomainEvent {

    private final Long userId;
    private final String loginName;

    public UserDeletedEvent(Long userId, String loginName) {
        super("USER_DELETED");
        this.userId = userId;
        this.loginName = loginName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginName() {
        return loginName;
    }
}
