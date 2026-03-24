package com.pd.modules.system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

/**
 * Add missing menu items on startup if they don't exist
 */
@Configuration
public class MissingMenuInitializer {

    private static final Logger log = LoggerFactory.getLogger(MissingMenuInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Bean
    @Order(2)
    public CommandLineRunner addMissingMenus() {
        return args -> {
            log.info("=== Checking for missing menu items ===");
            
            int addedCount = 0;
            
            // Job Calendar - under Job Mgmt (parent_id=2, not 200!)
            if (!menuExists(2020L)) {
                jdbcTemplate.update(
                    "INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    2020L, "Job Calendar", 2L, 20, "/system/job-calendar", "", "C", "0", "1", "system:job:calendar", "fa fa-calendar", "0", "admin", LocalDateTime.now(), "Job Calendar View"
                );
                addRoleMenu(1L, 2020L);
                addedCount++;
                log.info("Added menu: Job Calendar (parent: Job Mgmt)");
            }
            
            // Holiday Calendar - under Job Mgmt (parent_id=2)
            if (!menuExists(2021L)) {
                jdbcTemplate.update(
                    "INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    2021L, "Holiday Calendar", 2L, 21, "/system/holiday-calendar", "", "C", "0", "1", "system:job:calendar", "fa fa-calendar", "0", "admin", LocalDateTime.now(), "Holiday Calendar Management"
                );
                addRoleMenu(1L, 2021L);
                addedCount++;
                log.info("Added menu: Holiday Calendar (parent: Job Mgmt)");
            }
            
            // Live Logs - under Job Mgmt (parent_id=2)
            if (!menuExists(2022L)) {
                jdbcTemplate.update(
                    "INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    2022L, "Live Logs", 2L, 22, "/system/job-logs", "", "C", "0", "1", "system:job:list", "fa fa-terminal", "0", "admin", LocalDateTime.now(), "Real-time Job Logs"
                );
                addRoleMenu(1L, 2022L);
                addedCount++;
                log.info("Added menu: Live Logs (parent: Job Mgmt)");
            }
            
            // Email Templates - under Job Mgmt (parent_id=2)
            if (!menuExists(2023L)) {
                jdbcTemplate.update(
                    "INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    2023L, "Email Templates", 2L, 23, "/system/email-templates", "", "C", "0", "1", "system:job:template", "fa fa-envelope", "0", "admin", LocalDateTime.now(), "Email Template Management"
                );
                addRoleMenu(1L, 2023L);
                addedCount++;
                log.info("Added menu: Email Templates (parent: Job Mgmt)");
            }
            
            // Email Config - under System Config (parent_id=103)
            if (!menuExists(1034L)) {
                jdbcTemplate.update(
                    "INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    1034L, "Email Config", 103L, 4, "/system/email-config", "", "C", "0", "1", "system:config:email", "fa fa-envelope", "0", "admin", LocalDateTime.now(), "Email Server Configuration"
                );
                addRoleMenu(1L, 1034L);
                addedCount++;
                log.info("Added menu: Email Config (parent: System Config)");
            }
            
            if (addedCount > 0) {
                log.info("=== Added {} missing menu items ===", addedCount);
                // Clear menu cache so new menus appear immediately
                try {
                    var menuCache = cacheManager.getCache("menuTree");
                    if (menuCache != null) {
                        menuCache.clear();
                        log.info("=== Menu cache cleared ===");
                    }
                } catch (Exception e) {
                    log.warn("Could not clear menu cache: {}", e.getMessage());
                }
            } else {
                log.info("=== All menu items exist - No changes needed ===");
            }
        };
    }

    private boolean menuExists(Long menuId) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_menu WHERE menu_id = ?", Integer.class, menuId);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void addRoleMenu(Long roleId, Long menuId) {
        try {
            jdbcTemplate.update("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)", roleId, menuId);
        } catch (Exception e) {
            // Ignore duplicate key errors
        }
    }
}
