package com.pd.gateway.system;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.api.SystemUserService;
import com.pd.modules.system.context.UserAuditContextHolder;
import com.pd.modules.system.domain.SysUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for user management.
 * Uses SystemUserService API for all operations.
 */
@RestController
@RequestMapping("/api/system/user")
public class SysUserController extends BaseController {

    private final SystemUserService userService;

    public SysUserController(SystemUserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        var users = userService.findAllActive();
        return success(users);
    }

    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping(value = "/{userId}")
    public AjaxResult getInfo(@PathVariable Long userId) {
        Optional<SysUser> user = userService.findById(userId);
        return user.map(this::success).orElseGet(() -> error("User not found"));
    }

    @PreAuthorize("hasAuthority('system:user:add')")
    @Log(title = "User Management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysUser user) {
        // Check if login name already exists
        if (userService.existsByLoginName(user.getLoginName())) {
            return error("Login name already exists");
        }

        // Set default values
        user.setUserType("00");
        user.setSex(user.getSex() != null ? user.getSex() : "0");
        user.setStatus(user.getStatus() != null ? user.getStatus() : "0");
        user.setCreateBy("admin");

        // Set default password if not provided
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }

        userService.createUser(user);
        return success("User added successfully");
    }

    @PreAuthorize("hasAuthority('system:user:edit')")
    @Log(title = "User Management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysUser user) {
        // Capture BEFORE state from database
        Optional<SysUser> existing = userService.findById(user.getUserId());
        if (!existing.isPresent()) {
            return error("User not found");
        }
        
        // Store before state for audit trail
        UserAuditContextHolder.setBeforeEntity(existing.get());

        user.setUpdateBy("admin");
        SysUser updated = userService.updateUser(user);
        
        return success("User updated successfully", updated);
    }

    @PreAuthorize("hasAuthority('system:user:remove')")
    @Log(title = "User Management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userId}")
    public AjaxResult remove(@PathVariable Long userId) {
        if (!userService.deleteUser(userId)) {
            return error("User not found");
        }
        return success("User deleted successfully");
    }
}
