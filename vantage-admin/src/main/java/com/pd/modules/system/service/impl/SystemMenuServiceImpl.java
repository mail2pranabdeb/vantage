package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemMenuService;
import com.pd.modules.system.api.dto.MenuDTO;
import com.pd.modules.system.domain.SysMenu;
import com.pd.modules.system.infrastructure.repository.SysMenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SystemMenuServiceImpl implements SystemMenuService {

    private final SysMenuRepository menuRepository;

    public SystemMenuServiceImpl(SysMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public List<MenuDTO> findAll() {
        return menuRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<MenuDTO> findAllActive() {
        return menuRepository.findAllActive().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<MenuDTO> findAllMenus() {
        return menuRepository.findAllMenus().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<MenuDTO> findTree() {
        List<SysMenu> allMenus = menuRepository.findAllOrderBySort();
        return buildTreeDTO(allMenus, 0L);
    }

    @Override
    public List<MenuDTO> findMenusByRoleId(Long roleId) {
        return menuRepository.findMenusByRoleId(roleId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<MenuDTO> findMenusByUserId(Long userId) {
        return menuRepository.findMenusByUserId(userId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<MenuDTO> findById(Long menuId) {
        return menuRepository.findById(menuId).map(this::toDTO);
    }

    @Override
    public List<MenuDTO> findByParentId(Long parentId) {
        return menuRepository.findByParentIdOrderBySort(parentId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public MenuDTO createMenu(MenuDTO menu) {
        SysMenu entity = toEntity(menu);
        SysMenu saved = menuRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    public MenuDTO updateMenu(MenuDTO menu) {
        SysMenu entity = toEntity(menu);
        SysMenu saved = menuRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    public boolean deleteMenu(Long menuId) {
        if (menuRepository.existsById(menuId)) {
            menuRepository.deleteById(menuId);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasChildMenu(Long menuId) {
        return !menuRepository.findByParentId(menuId).isEmpty();
    }

    @Override
    public boolean isMenuUsedByAnyRole(Long menuId) {
        return !menuRepository.findRolesUsingMenu(menuId).isEmpty();
    }

    @Override
    public List<String> findPermissionsByUserId(Long userId) {
        return menuRepository.findPermsByUserId(userId);
    }

    @Override
    public List<MenuDTO> findNormalMenus() {
        return menuRepository.findNormalMenus().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private List<MenuDTO> buildTreeDTO(List<SysMenu> menus, Long parentId) {
        return menus.stream()
            .filter(m -> parentId.equals(m.getParentId()))
            .map(m -> {
                MenuDTO dto = toDTO(m);
                dto.setChildren(buildTreeDTO(menus, m.getMenuId()));
                return dto;
            })
            .collect(Collectors.toList());
    }

    private MenuDTO toDTO(SysMenu entity) {
        if (entity == null) return null;
        MenuDTO dto = new MenuDTO();
        dto.setMenuId(entity.getMenuId());
        dto.setMenuName(entity.getMenuName());
        dto.setParentId(entity.getParentId());
        dto.setOrderNum(entity.getOrderNum());
        dto.setUrl(entity.getUrl());
        dto.setTarget(entity.getTarget());
        dto.setMenuType(entity.getMenuType());
        dto.setVisible(entity.getVisible());
        dto.setIsRefresh(entity.getIsRefresh());
        dto.setPerms(entity.getPerms());
        dto.setIcon(entity.getIcon());
        dto.setStatus(entity.getStatus());
        dto.setCreateBy(entity.getCreateBy());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateBy(entity.getUpdateBy());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    private SysMenu toEntity(MenuDTO dto) {
        if (dto == null) return null;
        SysMenu entity = new SysMenu();
        entity.setMenuId(dto.getMenuId());
        entity.setMenuName(dto.getMenuName());
        entity.setParentId(dto.getParentId());
        entity.setOrderNum(dto.getOrderNum());
        entity.setUrl(dto.getUrl());
        entity.setTarget(dto.getTarget());
        entity.setMenuType(dto.getMenuType());
        entity.setVisible(dto.getVisible());
        entity.setIsRefresh(dto.getIsRefresh());
        entity.setPerms(dto.getPerms());
        entity.setIcon(dto.getIcon());
        entity.setStatus(dto.getStatus());
        entity.setCreateBy(dto.getCreateBy());
        entity.setCreateTime(dto.getCreateTime());
        entity.setUpdateBy(dto.getUpdateBy());
        entity.setUpdateTime(dto.getUpdateTime());
        entity.setRemark(dto.getRemark());
        return entity;
    }
}
