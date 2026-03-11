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
        return success("Role info retrieved");
    }

    @PreAuthorize("hasAuthority('system:role:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysRole role) {
        return success("Role added");
    }

    @PreAuthorize("hasAuthority('system:role:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysRole role) {
        return success("Role updated");
    }

    @PreAuthorize("hasAuthority('system:role:remove')")
    @DeleteMapping("/{roleIds}")
    public AjaxResult remove(@PathVariable Long[] roleIds) {
        return success("Role deleted");
    }
}
