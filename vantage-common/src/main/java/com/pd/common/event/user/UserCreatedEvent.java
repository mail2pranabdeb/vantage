package com.pd.common.event.user;

import com.pd.common.event.DomainEvent;

/**
 * Event published when a user is created.
 * Other modules can listen to this event for cross-module operations.
 */
public class UserCreatedEvent extends DomainEvent {

    private final Long userId;
    private final String loginName;
    private final String userName;

    public UserCreatedEvent(Long userId, String loginName, String userName) {
        super("USER_CREATED");
        this.userId = userId;
        this.loginName = loginName;
        this.userName = userName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getUserName() {
        return userName;
    }
}
