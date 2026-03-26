package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Long> {

    /**
     * Find all menu IDs for a role
     */
    @Query("SELECT rm.menuId FROM SysRoleMenu rm WHERE rm.roleId = :roleId")
    List<Long> findMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * Delete all role-menu associations for a role
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SysRoleMenu rm WHERE rm.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * Insert role-menu association
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO sys_role_menu (role_id, menu_id) VALUES (:roleId, :menuId)", nativeQuery = true)
    void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}
