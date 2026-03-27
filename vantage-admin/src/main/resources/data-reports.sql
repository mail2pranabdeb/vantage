-- Report Management Module Menu Initialization
-- First delete existing permission buttons if they exist
DELETE FROM sys_role_menu WHERE menu_id IN (5002, 5003, 5004, 5005, 5006);
DELETE FROM sys_menu WHERE menu_id IN (5002, 5003, 5004, 5005, 5006);

-- Add Report Management as top-level module (ID: 5000)
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (5000, 'Report Management', 0, 4, '#', '', 'M', '0', '1', '', 'fa fa-file-text', '0', 'admin', CURRENT_TIMESTAMP, 'Report Management Module');

-- Add Report List submenu (ID: 5001)
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (5001, 'Report List', 5000, 1, '/system/report', '', 'C', '0', '1', 'system:report:list', 'fa fa-list', '0', 'admin', CURRENT_TIMESTAMP, 'View and manage reports');

-- Add permission buttons UNDER Report List (parent_id=5001), visible='0' (shown in menu)
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (5002, 'Report Query', 5001, 1, '#', '', 'F', '0', '1', 'system:report:query', '', '0', 'admin', CURRENT_TIMESTAMP, 'Query report');

MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (5003, 'Report Add', 5001, 2, '#', '', 'F', '0', '1', 'system:report:add', '', '0', 'admin', CURRENT_TIMESTAMP, 'Add report');

MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (5004, 'Report Edit', 5001, 3, '#', '', 'F', '0', '1', 'system:report:edit', '', '0', 'admin', CURRENT_TIMESTAMP, 'Edit report');

MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (5005, 'Report Remove', 5001, 4, '#', '', 'F', '0', '1', 'system:report:remove', '', '0', 'admin', CURRENT_TIMESTAMP, 'Delete report');

MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (5006, 'Report Execute', 5001, 5, '#', '', 'F', '0', '1', 'system:report:execute', '', '0', 'admin', CURRENT_TIMESTAMP, 'Execute report');

-- Add role-menu permissions for admin role (role_id=1)
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5000);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5001);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5002);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5003);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5004);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5005);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5006);
