package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    @Query("SELECT r FROM SysRole r WHERE r.status = '0' AND r.delFlag = '0' ORDER BY r.roleSort")
    List<SysRole> findAllActive();

    Optional<SysRole> findByRoleKey(String roleKey);

    @Query("SELECT r FROM SysRole r WHERE r.status = :status")
    List<SysRole> findByStatus(@Param("status") String status);

    @Query(value = "SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.role_id = ur.role_id " +
            "WHERE ur.user_id = :userId", nativeQuery = true)
    List<SysRole> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM SysRole r WHERE r.status = :status")
    int countByStatus(@Param("status") String status);
}