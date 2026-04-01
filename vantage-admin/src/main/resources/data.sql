-- 1. Initial Data

-- Using BCrypt hash for 123456 as default for Spring Security
-- MERGE works in H2 Oracle mode
MERGE INTO sys_user KEY(login_name) VALUES(1, 'admin', 'Admin', '00', 'admin@bms.vip', '15888888888', '1', '', '$2a$10$CUmdVx1.RaVkRGu.pISr3.8/iPWinkuYQb.Jk7G2b4FVnk7qbUJsa', '', '0', '0', '127.0.0.1', null, null, 'admin', current_timestamp, '', null, 'Administrator');
MERGE INTO sys_user KEY(login_name) VALUES(2, 'prihan', 'Prihan', '00', 'prihan@qq.com', '15666666666', '1', '', '$2a$10$CUmdVx1.RaVkRGu.pISr3.8/iPWinkuYQb.Jk7G2b4FVnk7qbUJsa', '', '0', '0', '127.0.0.1', null, null, 'admin', current_timestamp, '', null, 'Tester');

insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, update_by, update_time, remark)
values(1, 'Super Admin', 'admin',  1, '1', '0', '0', 'admin', current_timestamp, '', null, 'Super Administrator');
insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, update_by, update_time, remark)
values(2, 'Common User', 'common', 2, '2', '0', '0', 'admin', current_timestamp, '', null, 'Common User Role');

-- ==================== SYSTEM MODULE MENUS ====================
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1, 'System Management', 0, 1, '#', '', 'M', '0', '1', '', 'fa fa-gear', '0', 'admin', current_timestamp, '', null, 'System Management');

-- User Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(11, 'User Mgmt', 1, 1, '/system/user', '', 'C', '0', '1', 'system:user:view', 'fa fa-user-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(111, 'User Query', 11, 1, '#', '', 'F', '0', '1', 'system:user:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(112, 'User Add', 11, 2, '#', '', 'F', '0', '1', 'system:user:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(113, 'User Update', 11, 3, '#', '', 'F', '0', '1', 'system:user:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(114, 'User Delete', 11, 4, '#', '', 'F', '0', '1', 'system:user:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(115, 'User Export', 11, 5, '#', '', 'F', '0', '1', 'system:user:export', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(116, 'User Import', 11, 6, '#', '', 'F', '0', '1', 'system:user:import', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(117, 'Reset Password', 11, 7, '#', '', 'F', '0', '1', 'system:user:resetPwd', '#', '0', 'admin', current_timestamp, '', null, '');

-- Role Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(12, 'Role Mgmt', 1, 2, '/system/role', '', 'C', '0', '1', 'system:role:view', 'fa fa-user-secret', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(121, 'Role Query', 12, 1, '#', '', 'F', '0', '1', 'system:role:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(122, 'Role Add', 12, 2, '#', '', 'F', '0', '1', 'system:role:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(123, 'Role Update', 12, 3, '#', '', 'F', '0', '1', 'system:role:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(124, 'Role Delete', 12, 4, '#', '', 'F', '0', '1', 'system:role:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(125, 'Role Export', 12, 5, '#', '', 'F', '0', '1', 'system:role:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Menu Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(13, 'Menu Mgmt', 1, 3, '/system/menu', '', 'C', '0', '1', 'system:menu:view', 'fa fa-th-list', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(131, 'Menu Query', 13, 1, '#', '', 'F', '0', '1', 'system:menu:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(132, 'Menu Add', 13, 2, '#', '', 'F', '0', '1', 'system:menu:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(133, 'Menu Update', 13, 3, '#', '', 'F', '0', '1', 'system:menu:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(134, 'Menu Delete', 13, 4, '#', '', 'F', '0', '1', 'system:menu:remove', '#', '0', 'admin', current_timestamp, '', null, '');

-- Config Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(14, 'Config Mgmt', 1, 4, '/system/config', '', 'C', '0', '1', 'system:config:view', 'fa fa-sun-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(141, 'Config Query', 14, 1, '#', '', 'F', '0', '1', 'system:config:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(142, 'Config Add', 14, 2, '#', '', 'F', '0', '1', 'system:config:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(143, 'Config Update', 14, 3, '#', '', 'F', '0', '1', 'system:config:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(144, 'Config Delete', 14, 4, '#', '', 'F', '0', '1', 'system:config:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(145, 'Config Export', 14, 5, '#', '', 'F', '0', '1', 'system:config:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Dict Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(15, 'Dict Mgmt', 1, 5, '/system/dict', '', 'C', '0', '1', 'system:dict:view', 'fa fa-bookmark-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(151, 'Dict Query', 15, 1, '#', '', 'F', '0', '1', 'system:dict:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(152, 'Dict Add', 15, 2, '#', '', 'F', '0', '1', 'system:dict:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(153, 'Dict Update', 15, 3, '#', '', 'F', '0', '1', 'system:dict:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(154, 'Dict Delete', 15, 4, '#', '', 'F', '0', '1', 'system:dict:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(155, 'Dict Export', 15, 5, '#', '', 'F', '0', '1', 'system:dict:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Login Info (Monitor)
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(16, 'Login Info', 1, 7, '/system/logininfor', '', 'C', '0', '1', 'system:logininfor:view', 'fa fa-file-image-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(161, 'Login Info Query', 16, 1, '#', '', 'F', '0', '1', 'system:logininfor:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(162, 'Login Info Delete', 16, 2, '#', '', 'F', '0', '1', 'system:logininfor:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(163, 'Login Info Clean', 16, 3, '#', '', 'F', '0', '1', 'system:logininfor:clean', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(164, 'Login Info Export', 16, 4, '#', '', 'F', '0', '1', 'system:logininfor:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Operation Log (Monitor)
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(17, 'Oper Log', 1, 8, '/system/operlog', '', 'C', '0', '1', 'system:operlog:view', 'fa fa-file-image-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(171, 'Oper Log Query', 17, 1, '#', '', 'F', '0', '1', 'system:operlog:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(172, 'Oper Log Delete', 17, 2, '#', '', 'F', '0', '1', 'system:operlog:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(173, 'Oper Log Clean', 17, 3, '#', '', 'F', '0', '1', 'system:operlog:clean', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(174, 'Oper Log Export', 17, 4, '#', '', 'F', '0', '1', 'system:operlog:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Notice Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(18, 'Notice Mgmt', 1, 9, '/system/notice', '', 'C', '0', '1', 'system:notice:view', 'fa fa-bullhorn', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(181, 'Notice Query', 18, 1, '#', '', 'F', '0', '1', 'system:notice:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(182, 'Notice Add', 18, 2, '#', '', 'F', '0', '1', 'system:notice:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(183, 'Notice Update', 18, 3, '#', '', 'F', '0', '1', 'system:notice:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(184, 'Notice Delete', 18, 4, '#', '', 'F', '0', '1', 'system:notice:remove', '#', '0', 'admin', current_timestamp, '', null, '');

-- Cache Management (under System Mgmt)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
VALUES (19, 'Cache Mgmt', 1, 5, '/system/cache', '', 'C', '0', '1', 'system:config:cache', 'fa fa-database', '0', 'admin', CURRENT_TIMESTAMP, 'Cache Management');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
VALUES (191, 'Cache Query', 1035, 1, '#', '', 'F', '1', '1', 'system:config:cache:list', '', '0', 'admin', CURRENT_TIMESTAMP, 'Query cache');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
VALUES (192, 'Cache Clear', 1035, 2, '#', '', 'F', '1', '1', 'system:config:cache:clear', '', '0', 'admin', CURRENT_TIMESTAMP, 'Clear cache');


-- ==================== QUARTZ MODULE MENUS ====================
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2, 'Job Management', 0, 2, '#', '', 'M', '0', '1', '', 'fa fa-tasks', '0', 'admin', current_timestamp, '', null, 'Scheduled Job Management');

-- Job Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(21, 'Job List', 2, 1, '/system/job', '', 'C', '0', '1', 'system:job:view', 'fa fa-clock-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(211, 'Job Query', 21, 1, '#', '', 'F', '0', '1', 'system:job:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(212, 'Job Add', 21, 2, '#', '', 'F', '0', '1', 'system:job:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(213, 'Job Update', 21, 3, '#', '', 'F', '0', '1', 'system:job:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(214, 'Job Delete', 21, 4, '#', '', 'F', '0', '1', 'system:job:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(215, 'Job Change Status', 21, 5, '#', '', 'F', '0', '1', 'system:job:changeStatus', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(216, 'Job Run', 21, 6, '#', '', 'F', '0', '1', 'system:job:run', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(217, 'Job Pause', 21, 7, '#', '', 'F', '0', '1', 'system:job:pause', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(218, 'Job Resume', 21, 8, '#', '', 'F', '0', '1', 'system:job:resume', '#', '0', 'admin', current_timestamp, '', null, '');

-- Job Log
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(22, 'Job Log', 2, 2, '/system/jobLog', '', 'C', '0', '1', 'system:jobLog:view', 'fa fa-file-text-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(221, 'Job Log Query', 22, 1, '#', '', 'F', '0', '1', 'system:jobLog:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(222, 'Job Log Delete', 22, 2, '#', '', 'F', '0', '1', 'system:jobLog:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(223, 'Job Log Clean', 22, 3, '#', '', 'F', '0', '1', 'system:jobLog:clean', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(224, 'Job Log Export', 22, 4, '#', '', 'F', '0', '1', 'system:jobLog:export', '#', '0', 'admin', current_timestamp, '', null, '');
-- Job Calendar menu (under Job Mgmt parent_id=2)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(23, 'Job Calendar', 2, 20, '/system/job-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', current_timestamp, '', null, 'Job Calendar View');

-- Holiday Calendar menu
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(24, 'Holiday Calendar', 2, 21, '/system/holiday-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', current_timestamp, '', null, 'Holiday Calendar Management');

-- Live Job Logs menu
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(25, 'Live Logs', 2, 22, '/system/job-logs', '', 'C', '0', '1', 'system:job:list', 'fa fa-terminal', '0', 'admin', current_timestamp, '', null, 'Real-time Job Logs');

-- Email Templates menu
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(26, 'Email Templates', 2, 23, '/system/email-templates', '', 'C', '0', '1', 'system:job:template', 'fa fa-envelope', '0', 'admin', current_timestamp, '', null, 'Email Template Management');

-- ==================== GENERATOR MODULE MENUS ====================
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(3, 'Code Gen Management', 0, 3, '#', '', 'M', '0', '1', '', 'fa fa-code', '0', 'admin', current_timestamp, '', null, 'Code Generator');

-- Code Generation
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(31, 'Code Gen', 3, 1, '/tool/gen', '', 'C', '0', '1', 'tool:gen:view', 'fa fa-code', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(311, 'Code Gen Query', 31, 1, '#', '', 'F', '0', '1', 'tool:gen:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(312, 'Code Gen Update', 31, 2, '#', '', 'F', '0', '1', 'tool:gen:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(313, 'Code Gen Delete', 31, 3, '#', '', 'F', '0', '1', 'tool:gen:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(314, 'Code Gen Import', 31, 4, '#', '', 'F', '0', '1', 'tool:gen:import', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(315, 'Code Gen Preview', 31, 5, '#', '', 'F', '0', '1', 'tool:gen:preview', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(316, 'Code Gen Download', 31, 6, '#', '', 'F', '0', '1', 'tool:gen:download', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(317, 'Code Gen Sync', 31, 7, '#', '', 'F', '0', '1', 'tool:gen:synchDb', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(318, 'Code Gen Batch', 31, 8, '#', '', 'F', '0', '1', 'tool:gen:batchGen', '#', '0', 'admin', current_timestamp, '', null, '');



-- Add menu permissions for admin role (role_id=1)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,1);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,2);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,3);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,11);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,12);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,13);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,14);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,15);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,16);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,17);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,18);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,19);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,21);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,22);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,23);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,24);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,25);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,26);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,31);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,111);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,112);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,113);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,114);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,115);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,116);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,117);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,121);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,122);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,123);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,124);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,125);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,131);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,132);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,133);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,134);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,141);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,142);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,143);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,144);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,145);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,151);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,152);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,153);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,154);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,155);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,161);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,162);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,163);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,164);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,171);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,172);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,173);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,174);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,181);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,182);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,183);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,184);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,211);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,212);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,213);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,214);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,215);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,216);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,217);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,218);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,221);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,222);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,223);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,224);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,311);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,312);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,313);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,314);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,315);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,316);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,317);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ( 1,318);


-- ==================== USER-ROLE ASSIGNMENTS ====================
-- Admin user gets admin role
insert into sys_user_role (user_id, role_id) values (1, 1);
-- Common user gets common role
insert into sys_user_role (user_id, role_id) values (2, 2);

-- ==================== DICTIONARY DATA ====================
-- Dictionary Types
insert into sys_dict_type (dict_type, dict_name, status, create_by, create_time, remark) values ('sys_normal_disable', 'Normal/Disabled Status', '0', 'admin', current_timestamp, 'System status options');
insert into sys_dict_type (dict_type, dict_name, status, create_by, create_time, remark) values ('sys_user_sex', 'User Gender', '0', 'admin', current_timestamp, 'Gender options');
insert into sys_dict_type (dict_type, dict_name, status, create_by, create_time, remark) values ('sys_yes_no', 'Yes/No Options', '0', 'admin', current_timestamp, 'Boolean options');
insert into sys_dict_type (dict_type, dict_name, status, create_by, create_time, remark) values ('sys_job_group', 'Job Groups', '0', 'admin', current_timestamp, 'Quartz job groups');

-- Dictionary Data - Normal/Disable Status
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) values (1, 'Normal', '0', 'sys_normal_disable', '0', 'admin', current_timestamp, 'Normal status');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) values (2, 'Disabled', '1', 'sys_normal_disable', '0', 'admin', current_timestamp, 'Disabled status');

-- Dictionary Data - User Gender
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) values (1, 'Unknown', '0', 'sys_user_sex', '0', 'admin', current_timestamp, 'Unknown gender');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) values (2, 'Male', '1', 'sys_user_sex', '0', 'admin', current_timestamp, 'Male gender');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) values (3, 'Female', '2', 'sys_user_sex', '0', 'admin', current_timestamp, 'Female gender');

-- Dictionary Data - Yes/No
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) values (1, 'Yes', 'Y', 'sys_yes_no', '0', 'admin', current_timestamp, 'Yes option');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) values (2, 'No', 'N', 'sys_yes_no', '0', 'admin', current_timestamp, 'No option');

-- Dictionary Data - Job Groups
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) values (1, 'Default', 'DEFAULT', 'sys_job_group', '0', 'admin', current_timestamp, 'Default group');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) values (2, 'System', 'SYSTEM', 'sys_job_group', '0', 'admin', current_timestamp, 'System group');

-- Note: Login records and operation logs are now automatically generated by the system
-- Sample data removed to avoid primary key conflicts with auto-generated IDs
-- Clear existing test data on restart
DELETE FROM sys_oper_log;
DELETE FROM sys_logininfor;
-- =====================================================
-- PENDING JOB FEATURES IMPLEMENTATION
-- =====================================================

-- Insert sample holidays
INSERT INTO sys_holiday (holiday_name, holiday_date, holiday_type, description, status) VALUES
('New Year''s Day', '2026-01-01', '1', 'National Holiday', '0'),
('Independence Day', '2026-07-04', '1', 'National Holiday', '0'),
('Christmas Day', '2026-12-25', '1', 'National Holiday', '0'),
('Company Anniversary', '2026-03-22', '2', 'Company Founding Day', '0'),
('Team Building Day', '2026-06-15', '2', 'Company Event', '0');

-- Insert job-role access mappings (admin role can manage all jobs)
INSERT INTO sys_job_role (job_id, role_id) VALUES
(1, 1), -- Job 1 accessible by Admin role
(2, 1), -- Job 2 accessible by Admin role
(3, 1); -- Job 3 accessible by Admin role



  
-- Multi-Datasource Menu  
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (28, 'Multi-Datasource', 1, 10, '/system/datasource', '', 'C', '0', '1', 'system:datasource:list', 'fa fa-database', '0', 'admin', CURRENT_TIMESTAMP, 'Multi-Datasource Management');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (281, 'Datasource Query', 28, 1, '#', '', 'F', '1', '1', 'system:datasource:query', '', '0', 'admin', CURRENT_TIMESTAMP, 'Query datasource');  
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (282, 'Datasource Add', 28, 2, '#', '', 'F', '1', '1', 'system:datasource:add', '', '0', 'admin', CURRENT_TIMESTAMP, 'Add datasource');  
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (283, 'Datasource Edit', 28, 3, '#', '', 'F', '1', '1', 'system:datasource:edit', '', '0', 'admin', CURRENT_TIMESTAMP, 'Edit datasource');  
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (284, 'Datasource Remove', 28, 4, '#', '', 'F', '1', '1', 'system:datasource:remove', '', '0', 'admin', CURRENT_TIMESTAMP, 'Delete datasource');  
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark) VALUES (285, 'Datasource Test', 28, 5, '#', '', 'F', '1', '1', 'system:datasource:test', '', '0', 'admin', CURRENT_TIMESTAMP, 'Test datasource connection');  
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 28);  
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 281);  
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 282);  
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 283);  
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 284);  
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 285);  
  
-- GLUE Job Script Menu  
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
VALUES (36, 'Script Jobs', 2, 6, '/system/script-job', '', 'C', '0', '1', 'system:scriptJob:list', 'fa fa-file-code', '0', 'admin', CURRENT_TIMESTAMP, 'GLUE-like Script Jobs');
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 36);  
