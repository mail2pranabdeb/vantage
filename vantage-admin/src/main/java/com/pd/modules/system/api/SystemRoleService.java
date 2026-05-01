package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.RoleDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * System module public API for role operations.
 */
public interface SystemRoleService {

    List<RoleDTO> findAll();

    List<RoleDTO> findAllActive();

    Optional<RoleDTO> findById(Long roleId);

    Optional<RoleDTO> findByRoleKey(String roleKey);

    RoleDTO createRole(RoleDTO role);

    RoleDTO updateRole(RoleDTO role);

    boolean deleteRole(Long roleId);

    boolean deleteRoleByIds(Long[] roleIds);

    boolean existsByRoleKey(String roleKey);

    void changeStatus(Long roleId, String status);

    void assignMenuPermissions(Long roleId, Long[] menuIds);

    void assignDataScope(Long roleId, String dataScope, Long[] deptIds);

    List<RoleDTO> findRolesByUserId(Long userId);

    Map<String, Object> findRoleWithPermissions(Long roleId);

    int countByStatus(String status);
}
