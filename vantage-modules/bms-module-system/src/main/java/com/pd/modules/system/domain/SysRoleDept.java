package com.pd.modules.system.domain;

import java.io.Serializable;

/**
 * Role-Dept relationship entity - sys_role_dept
 */
public class SysRoleDept implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Role ID */
    private Long roleId;

    /** Dept ID */
    private Long deptId;

    public SysRoleDept() {
    }

    public SysRoleDept(Long roleId, Long deptId) {
        this.roleId = roleId;
        this.deptId = deptId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }
}
