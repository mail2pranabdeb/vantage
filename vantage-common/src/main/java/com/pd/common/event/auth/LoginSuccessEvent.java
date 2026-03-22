package com.pd.common.event.auth;

import com.pd.common.event.DomainEvent;

/**
 * Event published when a user login succeeds.
 * Listeners can use this to record login success in sys_logininfor.
 */
public class LoginSuccessEvent extends DomainEvent {

    private final String loginName;
    private final String ipAddress;
    private final String location;
    private final String browser;
    private final String os;

    public LoginSuccessEvent(String loginName, String ipAddress, String location, String browser, String os) {
        super("LOGIN_SUCCESS");
        this.loginName = loginName;
        this.ipAddress = ipAddress;
        this.location = location;
        this.browser = browser;
        this.os = os;
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
}
