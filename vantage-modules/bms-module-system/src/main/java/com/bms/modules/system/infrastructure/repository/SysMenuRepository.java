package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysMenu;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.RowMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class SysMenuRepository {
    private final JdbcTemplate jdbcTemplate;

    public SysMenuRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysMenu> rowMapper = (rs, rowNum) -> {
        SysMenu menu = new SysMenu();
        menu.setMenuId(rs.getLong("menu_id"));
        menu.setMenuName(rs.getString("menu_name"));
        menu.setParentId(rs.getLong("parent_id"));
        menu.setOrderNum(rs.getInt("order_num"));
        menu.setUrl(rs.getString("url"));
        menu.setTarget(rs.getString("target"));
        menu.setMenuType(rs.getString("menu_type"));
        menu.setVisible(rs.getString("visible"));
        menu.setIsRefresh(rs.getString("is_refresh"));
        menu.setPerms(rs.getString("perms"));
        menu.setIcon(rs.getString("icon"));
        return menu;
    };

    public List<SysMenu> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_menu ORDER BY parent_id, order_num", rowMapper);
    }

    @Cacheable(value = "menuTree", key = "#userId")
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> menus;
        if (userId == 1L) {
            menus = jdbcTemplate.query(
                    "SELECT * FROM sys_menu WHERE menu_type IN ('M', 'C') AND status = '0' ORDER BY parent_id, order_num",
                    rowMapper);
        } else {
            String sql = "SELECT m.* FROM sys_menu m " +
                    "JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
                    "JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
                    "WHERE ur.user_id = ? AND m.menu_type IN ('M', 'C') AND m.status = '0' " +
                    "ORDER BY m.parent_id, m.order_num";
            menus = jdbcTemplate.query(sql, rowMapper, userId);
        }
        return getChildPerms(menus, 0);
    }

    private List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
        List<SysMenu> returnList = new ArrayList<>();
        for (SysMenu t : list) {
            if (t.getParentId() == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    private void recursionFn(List<SysMenu> list, SysMenu t) {
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
        List<SysMenu> tlist = new ArrayList<>();
        for (SysMenu n : list) {
            if (n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    private boolean hasChild(List<SysMenu> list, SysMenu t) {
        return getChildList(list, t).size() > 0;
    }

    public Set<String> findPermsByUserId(Long userId) {
        String sql = "SELECT m.perms FROM sys_menu m " +
                "JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
                "JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
                "WHERE ur.user_id = ? AND m.status = '0'";
        List<String> perms = jdbcTemplate.queryForList(sql, String.class, userId);
        return perms.stream()
                .filter(p -> p != null && !p.trim().isEmpty())
                .collect(Collectors.toSet());
    }

    public Set<String> findAllPerms() {
        String sql = "SELECT perms FROM sys_menu WHERE status = '0'";
        List<String> perms = jdbcTemplate.queryForList(sql, String.class);
        return perms.stream()
                .filter(p -> p != null && !p.trim().isEmpty())
                .collect(Collectors.toSet());
    }

    public SysMenu findById(Long menuId) {
        List<SysMenu> menus = jdbcTemplate.query(
                "SELECT * FROM sys_menu WHERE menu_id = ?",
                rowMapper,
                menuId);
        return menus.stream().findFirst().orElse(null);
    }

    public int insert(SysMenu menu) {
        String sql = "INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)";
        return jdbcTemplate.update(sql,
                menu.getMenuName(),
                menu.getParentId() != null ? menu.getParentId() : 0L,
                menu.getOrderNum() != null ? menu.getOrderNum() : 0,
                menu.getUrl(),
                menu.getTarget(),
                menu.getMenuType(),
                menu.getVisible(),
                menu.getIsRefresh() != null ? menu.getIsRefresh() : "1",
                menu.getPerms(),
                menu.getIcon(),
                "admin");
    }

    public int update(SysMenu menu) {
        String sql = "UPDATE sys_menu SET menu_name = ?, parent_id = ?, order_num = ?, url = ?, target = ?, menu_type = ?, visible = ?, is_refresh = ?, perms = ?, icon = ?, update_by = ?, update_time = current_timestamp WHERE menu_id = ?";
        return jdbcTemplate.update(sql,
                menu.getMenuName(),
                menu.getParentId(),
                menu.getOrderNum(),
                menu.getUrl(),
                menu.getTarget(),
                menu.getMenuType(),
                menu.getVisible(),
                menu.getIsRefresh(),
                menu.getPerms(),
                menu.getIcon(),
                "admin",
                menu.getMenuId());
    }

    public int deleteById(Long menuId) {
        return jdbcTemplate.update("DELETE FROM sys_menu WHERE menu_id = ?", menuId);
    }
}
