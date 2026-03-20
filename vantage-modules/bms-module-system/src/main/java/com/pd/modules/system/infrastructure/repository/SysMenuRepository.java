package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysMenu;
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

    // Get menu tree for specific user
    @Query(value = "SELECT DISTINCT m.* FROM sys_menu m " +
           "INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
           "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
           "WHERE ur.user_id = :userId AND m.status = '0' " +
           "ORDER BY m.parent_id, m.order_num", nativeQuery = true)
    List<SysMenu> findMenuTreeByUserId(@Param("userId") Long userId);

    // Get all permissions for specific user
    @Query(value = "SELECT DISTINCT m.perms FROM sys_menu m " +
           "INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
           "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
           "WHERE ur.user_id = :userId AND m.status = '0' AND m.perms IS NOT NULL AND m.perms <> ''", nativeQuery = true)
    Set<String> findMenuPermsByUserId(@Param("userId") Long userId);

    // Get all permissions (fallback)
    @Query(value = "SELECT DISTINCT m.perms FROM sys_menu m WHERE m.status = '0' AND m.perms IS NOT NULL AND m.perms <> ''", nativeQuery = true)
    Set<String> findAllPerms();
}
