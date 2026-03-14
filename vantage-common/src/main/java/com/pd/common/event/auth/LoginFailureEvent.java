package com.pd.common.event.auth;

import com.pd.common.event.DomainEvent;

/**
 * Event published when a user login fails.
 * Listeners can use this to record login failure in sys_logininfor.
 */
public class LoginFailureEvent extends DomainEvent {

    private final String loginName;
    private final String ipAddress;
    private final String location;
    private final String browser;
    private final String os;
    private final String message;

    public LoginFailureEvent(String loginName, String ipAddress, String location, String browser, String os, String message) {
        super("LOGIN_FAILURE");
        this.loginName = loginName;
        this.ipAddress = ipAddress;
        this.location = location;
        this.browser = browser;
        this.os = os;
        this.message = message;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public String getBrowser() {
        return browser;
    }

    public String getOs() {
        return os;
    }

    public String getMessage() {
        return message;
    }
}
