package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysRole;
import com.pd.modules.system.infrastructure.repository.SysRoleRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
        return success(roleRepository.findAll());
    }

    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable Long roleId) {
        SysRole role = roleRepository.findById(roleId);
        return role != null ? success(role) : error("Role not found");
    }

    @PreAuthorize("hasAuthority('system:role:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysRole role) {
        if (roleRepository.findById(role.getRoleId()) != null) {
            return error("Role key already exists");
        }
        
        role.setDataScope("1");
        role.setStatus(role.getStatus() != null ? role.getStatus() : "0");
        role.setDelFlag("0");
        
        roleRepository.save(role);
        return success("Role added successfully");
    }

    @PreAuthorize("hasAuthority('system:role:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysRole role) {
        SysRole existing = roleRepository.findById(role.getRoleId());
        if (existing == null) {
            return error("Role not found");
        }
        
        roleRepository.save(role);
        return success("Role updated successfully");
    }

    @PreAuthorize("hasAuthority('system:role:remove')")
    @DeleteMapping("/{roleId}")
    public AjaxResult remove(@PathVariable Long roleId) {
        SysRole role = roleRepository.findById(roleId);
        if (role == null) {
            return error("Role not found");
        }
        
        roleRepository.deleteById(roleId);
        return success("Role deleted successfully");
    }
}
