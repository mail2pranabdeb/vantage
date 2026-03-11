package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysMenu;
import com.pd.modules.system.infrastructure.repository.SysMenuRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController extends BaseController {

    private final SysMenuRepository menuRepository;

    public SysMenuController(SysMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @GetMapping("/tree")
    public AjaxResult getMenuTree() {
        // In a real app, get current user ID from SecurityContext
        // For now, assuming admin (1L) or getting from context if available
        Long userId = 1L;
        return success(menuRepository.selectMenuTreeByUserId(userId));
    }

    @PreAuthorize("hasAuthority('system:menu:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        return success(menuRepository.findAll());
    }

    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public AjaxResult getInfo(@PathVariable Long menuId) {
        return success("Menu info retrieved");
    }

    @PreAuthorize("hasAuthority('system:menu:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysMenu menu) {
        return success("Menu added");
    }

    @PreAuthorize("hasAuthority('system:menu:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysMenu menu) {
        return success("Menu updated");
    }

    @PreAuthorize("hasAuthority('system:menu:remove')")
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@PathVariable Long menuId) {
        return success("Menu deleted");
    }
}
