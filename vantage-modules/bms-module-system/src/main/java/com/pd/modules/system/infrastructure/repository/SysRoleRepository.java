package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    @Query("SELECT r FROM SysRole r WHERE r.status = '0' AND r.delFlag = '0' ORDER BY r.roleSort")
    List<SysRole> findAllActive();

    Optional<SysRole> findByRoleKey(String roleKey);
}