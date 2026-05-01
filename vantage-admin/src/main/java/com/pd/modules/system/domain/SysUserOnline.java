package com.pd.modules.system.domain;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * System user online entity - sys_user_online
 */
@Data
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

    public boolean isExpired() {
        return "expired".equals(status);
    }
}
