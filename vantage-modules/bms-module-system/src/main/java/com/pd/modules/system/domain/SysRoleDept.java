package com.pd.modules.system.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * Role-Dept relationship entity - sys_role_dept
 */
@Data
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
}
