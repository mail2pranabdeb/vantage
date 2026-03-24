-- =====================================================
-- ADD MISSING MENU ITEMS FOR NEW FEATURES
-- Run this to add menus to existing database
-- =====================================================

-- Job Calendar menu (under Job Mgmt parent_id=200)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(2020, 'Job Calendar', 200, 20, '/system/job-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', CURRENT_TIMESTAMP, '', NULL, 'Job Calendar View')
ON CONFLICT (menu_id) DO NOTHING;

-- Holiday Calendar menu
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(2021, 'Holiday Calendar', 200, 21, '/system/holiday-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', CURRENT_TIMESTAMP, '', NULL, 'Holiday Calendar Management')
ON CONFLICT (menu_id) DO NOTHING;

-- Live Job Logs menu
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(2022, 'Live Logs', 200, 22, '/system/job-logs', '', 'C', '0', '1', 'system:job:list', 'fa fa-terminal', '0', 'admin', CURRENT_TIMESTAMP, '', NULL, 'Real-time Job Logs')
ON CONFLICT (menu_id) DO NOTHING;

-- Email Templates menu
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(2023, 'Email Templates', 200, 23, '/system/email-templates', '', 'C', '0', '1', 'system:job:template', 'fa fa-envelope', '0', 'admin', CURRENT_TIMESTAMP, '', NULL, 'Email Template Management')
ON CONFLICT (menu_id) DO NOTHING;

-- Add menu permissions for admin role (role_id=1)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2020)
ON CONFLICT (role_id, menu_id) DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2021)
ON CONFLICT (role_id, menu_id) DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2022)
ON CONFLICT (role_id, menu_id) DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2023)
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- Email Config menu (under System Config parent_id=103)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(1034, 'Email Config', 103, 4, '/system/email-config', '', 'C', '0', '1', 'system:config:email', 'fa fa-envelope', '0', 'admin', CURRENT_TIMESTAMP, '', NULL, 'Email Server Configuration')
ON CONFLICT (menu_id) DO NOTHING;

-- Add menu permissions for admin role
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1034)
ON CONFLICT (role_id, menu_id) DO NOTHING;
