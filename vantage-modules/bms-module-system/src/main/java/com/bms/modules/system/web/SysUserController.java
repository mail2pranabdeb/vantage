package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysUser;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/user")
public class SysUserController extends BaseController {

    private final SysUserRepository userRepository;

    public SysUserController(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        return success(userRepository.findAll());
    }

    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping(value = "/{userId}")
    public AjaxResult getInfo(@PathVariable Long userId) {
        return success("User info retrieved");
    }

    @PreAuthorize("hasAuthority('system:user:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysUser user) {
        return success("User added");
    }

    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysUser user) {
        return success("User updated");
    }

    @PreAuthorize("hasAuthority('system:user:remove')")
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        return success("User deleted");
    }
}
