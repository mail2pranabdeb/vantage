package com.pd.modules.system.domain;

import java.io.Serializable;

/**
 * Role-Menu relationship entity - sys_role_menu
 */
public class SysRoleMenu implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Role ID */
    private Long roleId;

    /** Menu ID */
    private Long menuId;

    public SysRoleMenu() {
    }

    public SysRoleMenu(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
}
