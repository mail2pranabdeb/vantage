-- Insert new menus
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (2020, 'Job Calendar', 2, 20, '/system/job-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', CURRENT_TIMESTAMP, 'Job Calendar View');

MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (2021, 'Holiday Calendar', 2, 21, '/system/holiday-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', CURRENT_TIMESTAMP, 'Holiday Calendar Management');

MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (2022, 'Live Logs', 2, 22, '/system/job-logs', '', 'C', '0', '1', 'system:job:list', 'fa fa-terminal', '0', 'admin', CURRENT_TIMESTAMP, 'Real-time Job Logs');

MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (2023, 'Email Templates', 2, 23, '/system/email-templates', '', 'C', '0', '1', 'system:job:template', 'fa fa-envelope', '0', 'admin', CURRENT_TIMESTAMP, 'Email Template Management');

MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) 
VALUES (1034, 'Email Config', 103, 4, '/system/email-config', '', 'C', '0', '1', 'system:config:email', 'fa fa-envelope', '0', 'admin', CURRENT_TIMESTAMP, 'Email Server Configuration');

-- Add role-menu permissions for admin role (role_id=1)
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2020);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2021);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2022);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2023);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 1034);
