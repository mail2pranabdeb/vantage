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

    @Query("SELECT r FROM SysRole r WHERE r.roleKey = :roleKey AND r.delFlag = '0'")
    Optional<SysRole> findByRoleKey(@Param("roleKey") String roleKey);

    @Query("SELECT r FROM SysRole r WHERE r.delFlag = '0' ORDER BY r.roleSort ASC")
    List<SysRole> findAllActive();

    @Query("SELECT r FROM SysRole r WHERE r.status = :status AND r.delFlag = '0'")
    List<SysRole> findByStatus(@Param("status") String status);

    @Query("SELECT r FROM SysRole r JOIN SysUserRole ur ON r.roleId = ur.roleId WHERE ur.userId = :userId AND r.status = '0' AND r.delFlag = '0'")
    List<SysRole> findRolesByUserId(@Param("userId") Long userId);
}
