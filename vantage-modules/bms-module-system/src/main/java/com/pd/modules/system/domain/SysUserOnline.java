package com.pd.modules.system.domain;

import java.time.LocalDateTime;

/**
 * System user online entity - sys_user_online
 */
public class SysUserOnline {

    /** Session ID */
    private String sessionId;

    /** Login name */
    private String loginName;

    /** Department name */
    private String deptName;

    /** User IP */
    private String ipaddr;

    /** Login location */
    private String loginLocation;

    /** Browser type */
    private String browser;

    /** Operating system */
    private String os;

    /** Session status (online/expired) */
    private String status;

    /** Session start time */
    private LocalDateTime startTimestamp;

    /** Last access time */
    private LocalDateTime lastAccessTime;

    // Getters and Setters

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public String getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation) {
        this.loginLocation = loginLocation;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(LocalDateTime startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public boolean isExpired() {
        return "expired".equals(status);
    }
}
