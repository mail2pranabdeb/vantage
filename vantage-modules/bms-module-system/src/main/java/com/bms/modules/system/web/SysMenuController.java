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
        SysMenu menu = menuRepository.findById(menuId);
        return menu != null ? success(menu) : error("Menu not found");
    }

    @PreAuthorize("hasAuthority('system:menu:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysMenu menu) {
        menu.setVisible(menu.getVisible() != null ? menu.getVisible() : "0");
        menu.setMenuType(menu.getMenuType() != null ? menu.getMenuType() : "M");
        menuRepository.insert(menu);
        return success("Menu added successfully");
    }

    @PreAuthorize("hasAuthority('system:menu:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysMenu menu) {
        SysMenu existing = menuRepository.findById(menu.getMenuId());
        if (existing == null) {
            return error("Menu not found");
        }
        menuRepository.update(menu);
        return success("Menu updated successfully");
    }

    @PreAuthorize("hasAuthority('system:menu:remove')")
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@PathVariable Long menuId) {
        SysMenu menu = menuRepository.findById(menuId);
        if (menu == null) {
            return error("Menu not found");
        }
        menuRepository.deleteById(menuId);
        return success("Menu deleted successfully");
    }
}
