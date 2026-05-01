package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.MenuDTO;
import java.util.List;
import java.util.Optional;

public interface SystemMenuService {

    List<MenuDTO> findAll();

    List<MenuDTO> findAllActive();

    List<MenuDTO> findAllMenus();

    List<MenuDTO> findTree();

    List<MenuDTO> findMenusByRoleId(Long roleId);

    List<MenuDTO> findMenusByUserId(Long userId);

    Optional<MenuDTO> findById(Long menuId);

    List<MenuDTO> findByParentId(Long parentId);

    MenuDTO createMenu(MenuDTO menu);

    MenuDTO updateMenu(MenuDTO menu);

    boolean deleteMenu(Long menuId);

    boolean hasChildMenu(Long menuId);

    boolean isMenuUsedByAnyRole(Long menuId);

    List<String> findPermissionsByUserId(Long userId);

    List<MenuDTO> findNormalMenus();
}
