-- Add Cache Management Menu
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (1035, 'Cache Management', 103, 5, '/system/cache', '', 'C', '0', '1', 'system:config:cache', 'fa fa-database', '0', 'admin', CURRENT_TIMESTAMP, 'Cache Management');

MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 1035);

-- Verify
SELECT menu_id, menu_name, parent_id FROM sys_menu WHERE menu_id = 1035;
