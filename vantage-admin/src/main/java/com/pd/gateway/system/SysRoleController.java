package com.pd.gateway.system;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysRole;
import com.pd.modules.system.infrastructure.repository.SysRoleRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/system/role")
public class SysRoleController extends BaseController {

    private final SysRoleRepository roleRepository;

    public SysRoleController(SysRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        return success(roleRepository.findAllActive());
    }

    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable Long roleId) {
        Optional<SysRole> role = roleRepository.findById(roleId);
        return role.map(this::success).orElseGet(() -> error("Role not found"));
    }

    @PreAuthorize("hasAuthority('system:role:add')")
    @Log(title = "Role Management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysRole role) {
        Optional<SysRole> existing = roleRepository.findByRoleKey(role.getRoleKey());
        if (existing.isPresent()) {
            return error("Role key already exists");
        }

        role.setDataScope("1");
        role.setStatus(role.getStatus() != null ? role.getStatus() : "0");
        role.setDelFlag("0");
        role.setCreateBy("admin");
        role.setCreateTime(LocalDateTime.now());

        roleRepository.save(role);
        return success("Role added successfully");
    }

    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "Role Management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysRole role) {
        Optional<SysRole> existing = roleRepository.findById(role.getRoleId());
        if (!existing.isPresent()) {
            return error("Role not found");
        }

        role.setUpdateBy("admin");
        role.setUpdateTime(LocalDateTime.now());

        roleRepository.save(role);
        return success("Role updated successfully");
    }

    @PreAuthorize("hasAuthority('system:role:remove')")
    @Log(title = "Role Management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleId}")
    public AjaxResult remove(@PathVariable Long roleId) {
        Optional<SysRole> role = roleRepository.findById(roleId);
        if (!role.isPresent()) {
            return error("Role not found");
        }

        role.get().setDelFlag("2");
        role.get().setUpdateTime(LocalDateTime.now());
        role.get().setUpdateBy("admin");
        roleRepository.save(role.get());

        return success("Role deleted successfully");
    }
}
