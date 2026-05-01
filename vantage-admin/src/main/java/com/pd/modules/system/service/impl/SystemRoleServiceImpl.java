package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemRoleService;
import com.pd.modules.system.api.dto.RoleDTO;
import com.pd.modules.system.domain.SysRole;
import com.pd.modules.system.infrastructure.repository.SysRoleRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SystemRoleServiceImpl implements SystemRoleService {

    private final SysRoleRepository roleRepository;

    public SystemRoleServiceImpl(SysRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<RoleDTO> findAll() {
        return roleRepository.findAll().stream()
            .map(this::toDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<RoleDTO> findAllActive() {
        return roleRepository.findByStatus("0").stream()
            .map(this::toDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Optional<RoleDTO> findById(Long roleId) {
        return roleRepository.findById(roleId).map(this::toDTO);
    }

    @Override
    public Optional<RoleDTO> findByRoleKey(String roleKey) {
        return roleRepository.findByRoleKey(roleKey).map(this::toDTO);
    }

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        SysRole role = toEntity(roleDTO);
        return toDTO(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleDTO updateRole(RoleDTO roleDTO) {
        SysRole role = toEntity(roleDTO);
        return toDTO(roleRepository.save(role));
    }

    @Override
    @Transactional
    public boolean deleteRole(Long roleId) {
        if (roleRepository.existsById(roleId)) {
            roleRepository.deleteById(roleId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteRoleByIds(Long[] roleIds) {
        for (Long id : roleIds) {
            deleteRole(id);
        }
        return true;
    }

    @Override
    public boolean existsByRoleKey(String roleKey) {
        return roleRepository.findByRoleKey(roleKey).isPresent();
    }

    @Override
    @Transactional
    public void changeStatus(Long roleId, String status) {
        roleRepository.findById(roleId).ifPresent(role -> {
            role.setStatus(status);
            roleRepository.save(role);
        });
    }

    @Override
    @Transactional
    public void assignMenuPermissions(Long roleId, Long[] menuIds) {
        roleRepository.findById(roleId).ifPresent(role -> {
            role.setMenuIds(menuIds);
            roleRepository.save(role);
        });
    }

    @Override
    @Transactional
    public void assignDataScope(Long roleId, String dataScope, Long[] deptIds) {
        roleRepository.findById(roleId).ifPresent(role -> {
            role.setDataScope(dataScope);
            role.setDeptIds(deptIds);
            roleRepository.save(role);
        });
    }

    @Override
    public List<RoleDTO> findRolesByUserId(Long userId) {
        return roleRepository.findByUserId(userId).stream()
            .map(this::toDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Map<String, Object> findRoleWithPermissions(Long roleId) {
        Map<String, Object> result = new HashMap<>();
        Optional<RoleDTO> roleOpt = findById(roleId);
        roleOpt.ifPresent(role -> {
            result.put("role", role);
            result.put("menuIds", role.getMenuIds());
        });
        return result;
    }

    @Override
    public int countByStatus(String status) {
        return roleRepository.countByStatus(status);
    }

    private RoleDTO toDTO(SysRole entity) {
        RoleDTO dto = new RoleDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private SysRole toEntity(RoleDTO dto) {
        SysRole entity = new SysRole();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
