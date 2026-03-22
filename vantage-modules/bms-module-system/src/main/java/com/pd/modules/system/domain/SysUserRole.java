package com.pd.modules.system.domain;

import java.io.Serializable;

/**
 * User-Role relationship entity - sys_user_role
 */
public class SysUserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    /** User ID */
    private Long userId;

    /** Role ID */
    private Long roleId;

    public SysUserRole() {
    }

    public SysUserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
