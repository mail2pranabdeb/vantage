-- Report Management Module Menu Initialization
-- Uses MERGE with explicit column names to avoid column count mismatch

-- Add Report Management as top-level module (ID: 5000)
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (5000, 'Report Management', 0, 4, '#', '', 'M', '0', '1', '', 'fa fa-file-text', '0', 'admin', CURRENT_TIMESTAMP, 'Report Management Module');

-- Add Report List submenu (ID: 5001)
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (5001, 'Report List', 5000, 1, '/system/report', '', 'C', '0', '1', 'system:report:list', 'fa fa-list', '0', 'admin', CURRENT_TIMESTAMP, 'View and manage reports');

-- Add role-menu permissions for admin role (role_id=1)
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5000);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5001);
