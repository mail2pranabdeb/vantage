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

    @Query(value = "SELECT m.perms FROM sys_menu m WHERE m.status = '0' AND m.perms IS NOT NULL AND m.perms <> ''", nativeQuery = true)
    List<String> findAllPermsList();

    @Query(value = "SELECT m.* FROM sys_menu m WHERE m.status = '0' ORDER BY m.parent_id, m.order_num", nativeQuery = true)
    List<SysMenu> findAllMenus();
}