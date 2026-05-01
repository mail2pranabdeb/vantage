package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysMenu;
import com.pd.modules.system.domain.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {

    @Query("SELECT m FROM SysMenu m WHERE m.status = '0' ORDER BY m.parentId, m.orderNum")
    List<SysMenu> findAllActive();

    @Query("SELECT m FROM SysMenu m WHERE m.menuType IN ('M', 'C') AND m.status = '0' ORDER BY m.parentId, m.orderNum")
    List<SysMenu> findMenusAndDirectories();

    @Query(value = "SELECT m.perms FROM sys_menu m WHERE m.status = '0' AND m.perms IS NOT NULL AND m.perms <> ''", nativeQuery = true)
    List<String> findAllPermsList();

    @Query(value = "SELECT m.* FROM sys_menu m WHERE m.status = '0' ORDER BY m.parent_id, m.order_num", nativeQuery = true)
    List<SysMenu> findAllMenus();

    @Query("SELECT m FROM SysMenu m ORDER BY m.orderNum")
    List<SysMenu> findAllOrderBySort();

    @Query(value = "SELECT m.* FROM sys_menu m INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id WHERE rm.role_id = :roleId ORDER BY m.parent_id, m.order_num", nativeQuery = true)
    List<SysMenu> findMenusByRoleId(@Param("roleId") Long roleId);

    @Query(value = "SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = :userId ORDER BY m.parent_id, m.order_num", nativeQuery = true)
    List<SysMenu> findMenusByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM SysMenu m WHERE m.parentId = :parentId ORDER BY m.orderNum")
    List<SysMenu> findByParentIdOrderBySort(@Param("parentId") Long parentId);

    @Query("SELECT m FROM SysMenu m WHERE m.parentId = :parentId")
    List<SysMenu> findByParentId(@Param("parentId") Long parentId);

    @Query(value = "SELECT r.* FROM sys_role r INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id WHERE rm.menu_id = :menuId", nativeQuery = true)
    List<SysRole> findRolesUsingMenu(@Param("menuId") Long menuId);

    @Query(value = "SELECT DISTINCT m.perms FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = :userId AND m.perms IS NOT NULL AND m.perms <> ''", nativeQuery = true)
    List<String> findPermsByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM SysMenu m WHERE m.menuType IN ('M', 'C', 'F') AND m.status = '0' ORDER BY m.parentId, m.orderNum")
    List<SysMenu> findNormalMenus();
}