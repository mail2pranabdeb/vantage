package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {

    @Query("SELECT m FROM SysMenu m WHERE m.status = '0' ORDER BY m.parentId, m.orderNum")
    List<SysMenu> findAllActive();

    @Query("SELECT m FROM SysMenu m WHERE m.menuType IN ('M', 'C') AND m.status = '0' ORDER BY m.parentId, m.orderNum")
    List<SysMenu> findMenusAndDirectories();

    @Query("SELECT m FROM SysMenu m JOIN SysRoleMenu rm ON m.menuId = rm.menuId JOIN SysUserRole ur ON rm.roleId = ur.roleId WHERE ur.userId = :userId AND m.menuType IN ('M', 'C') AND m.status = '0' ORDER BY m.parentId, m.orderNum")
    List<SysMenu> findMenuTreeByUserId(@Param("userId") Long userId);

    @Query("SELECT m.perms FROM SysMenu m JOIN SysRoleMenu rm ON m.menuId = rm.menuId JOIN SysUserRole ur ON rm.roleId = ur.roleId WHERE ur.userId = :userId AND m.status = '0'")
    List<String> findPermsByUserId(@Param("userId") Long userId);

    @Query("SELECT m.perms FROM SysMenu m WHERE m.status = '0' AND m.perms IS NOT NULL AND m.perms <> ''")
    List<String> findAllPerms();
}
