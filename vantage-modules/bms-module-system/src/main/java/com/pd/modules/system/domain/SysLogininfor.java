package com.pd.modules.system.domain;

import java.time.LocalDateTime;

/**
 * System login info entity - sys_logininfor
 */
public class SysLogininfor {

    /** ID */
    private Long infoId;

    /** Login name */
    private String loginName;

    /** Login status (0 success, 1 failure) */
    private String status;

    /** IP address */
    private String ipaddr;

    /** Login location */
    private String loginLocation;

    /** Browser type */
    private String browser;

    /** Operating system */
    private String os;

    /** Message */
    private String msg;

    /** Login time */
    private LocalDateTime loginTime;

    // Getters and Setters

    public Long getInfoId() {
        return infoId;
    }

    public void setInfoId(Long infoId) {
        this.infoId = infoId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
}
