package com.pd.gateway.system;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysMenu;
import com.pd.modules.system.infrastructure.repository.SysMenuRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController extends BaseController {

    private final SysMenuRepository menuRepository;

    public SysMenuController(SysMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @GetMapping("/tree")
    public AjaxResult getMenuTree() {
        List<SysMenu> menus = menuRepository.findAllMenus();
        return success(buildMenuTree(menus, 0L));
    }

    @PreAuthorize("hasAuthority('system:menu:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        return success(menuRepository.findAllActive());
    }

    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public AjaxResult getInfo(@PathVariable Long menuId) {
        Optional<SysMenu> menu = menuRepository.findById(menuId);
        return menu.map(this::success).orElse(error("Menu not found"));
    }

    @PreAuthorize("hasAuthority('system:menu:add')")
    @Log(title = "Menu Management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysMenu menu) {
        menu.setVisible(menu.getVisible() != null ? menu.getVisible() : "0");
        menu.setMenuType(menu.getMenuType() != null ? menu.getMenuType() : "M");
        menu.setStatus("0");
        menu.setCreateBy("admin");
        menu.setCreateTime(LocalDateTime.now());
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getOrderNum() == null) {
            menu.setOrderNum(0);
        }
        menuRepository.save(menu);
        return success("Menu added successfully");
    }

    @PreAuthorize("hasAuthority('system:menu:edit')")
    @Log(title = "Menu Management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysMenu menu) {
        Optional<SysMenu> existing = menuRepository.findById(menu.getMenuId());
        if (!existing.isPresent()) {
            return error("Menu not found");
        }
        menu.setUpdateBy("admin");
        menu.setUpdateTime(LocalDateTime.now());
        menuRepository.save(menu);
        return success("Menu updated successfully");
    }

    @PreAuthorize("hasAuthority('system:menu:remove')")
    @Log(title = "Menu Management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@PathVariable Long menuId) {
        if (!menuRepository.findById(menuId).isPresent()) {
            return error("Menu not found");
        }
        menuRepository.deleteById(menuId);
        return success("Menu deleted successfully");
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        for (SysMenu menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                menu.setChildren(buildMenuTree(menus, menu.getMenuId()));
            }
        }
        return menus.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .toList();
    }
}
