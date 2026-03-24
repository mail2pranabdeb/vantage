-- 1. Initial Data

-- Using BCrypt hash for 123456 as default for Spring Security
insert into sys_user (user_id, login_name, user_name, user_type, email, phonenumber, sex, avatar, password, salt, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
values(1, 'admin', 'Admin', '00', 'admin@bms.vip', '15888888888', '1', '', '$2a$10$CUmdVx1.RaVkRGu.pISr3.8/iPWinkuYQb.Jk7G2b4FVnk7qbUJsa', '', '0', '0', '127.0.0.1', null, null, 'admin', current_timestamp, '', null, 'Administrator');
insert into sys_user (user_id, login_name, user_name, user_type, email, phonenumber, sex, avatar, password, salt, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
values(2, 'prihan',    'Prihan', '00', 'prihan@qq.com',  '15666666666', '1', '', '$2a$10$CUmdVx1.RaVkRGu.pISr3.8/iPWinkuYQb.Jk7G2b4FVnk7qbUJsa', '', '0', '0', '127.0.0.1', null, null, 'admin', current_timestamp, '', null, 'Tester');

insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, update_by, update_time, remark)
values(1, 'Super Admin', 'admin',  1, '1', '0', '0', 'admin', current_timestamp, '', null, 'Super Administrator');
insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, update_by, update_time, remark)
values(2, 'Common User', 'common', 2, '2', '0', '0', 'admin', current_timestamp, '', null, 'Common User Role');

-- ==================== SYSTEM MODULE MENUS ====================
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1, 'System Mgmt', 0, 1, '#', '', 'M', '0', '1', '', 'fa fa-gear', '0', 'admin', current_timestamp, '', null, 'System Management');

-- User Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(100, 'User Mgmt', 1, 1, '/system/user', '', 'C', '0', '1', 'system:user:view', 'fa fa-user-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1000, 'User Query', 100, 1, '#', '', 'F', '0', '1', 'system:user:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1001, 'User Add', 100, 2, '#', '', 'F', '0', '1', 'system:user:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1002, 'User Update', 100, 3, '#', '', 'F', '0', '1', 'system:user:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1003, 'User Delete', 100, 4, '#', '', 'F', '0', '1', 'system:user:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1004, 'User Export', 100, 5, '#', '', 'F', '0', '1', 'system:user:export', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1005, 'User Import', 100, 6, '#', '', 'F', '0', '1', 'system:user:import', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1006, 'Reset Password', 100, 7, '#', '', 'F', '0', '1', 'system:user:resetPwd', '#', '0', 'admin', current_timestamp, '', null, '');

-- Role Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(101, 'Role Mgmt', 1, 2, '/system/role', '', 'C', '0', '1', 'system:role:view', 'fa fa-user-secret', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1010, 'Role Query', 101, 1, '#', '', 'F', '0', '1', 'system:role:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1011, 'Role Add', 101, 2, '#', '', 'F', '0', '1', 'system:role:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1012, 'Role Update', 101, 3, '#', '', 'F', '0', '1', 'system:role:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1013, 'Role Delete', 101, 4, '#', '', 'F', '0', '1', 'system:role:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1014, 'Role Export', 101, 5, '#', '', 'F', '0', '1', 'system:role:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Menu Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(102, 'Menu Mgmt', 1, 3, '/system/menu', '', 'C', '0', '1', 'system:menu:view', 'fa fa-th-list', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1020, 'Menu Query', 102, 1, '#', '', 'F', '0', '1', 'system:menu:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1021, 'Menu Add', 102, 2, '#', '', 'F', '0', '1', 'system:menu:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1022, 'Menu Update', 102, 3, '#', '', 'F', '0', '1', 'system:menu:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1023, 'Menu Delete', 102, 4, '#', '', 'F', '0', '1', 'system:menu:remove', '#', '0', 'admin', current_timestamp, '', null, '');

-- Config Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(103, 'Config Mgmt', 1, 4, '/system/config', '', 'C', '0', '1', 'system:config:view', 'fa fa-sun-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1030, 'Config Query', 103, 1, '#', '', 'F', '0', '1', 'system:config:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1031, 'Config Add', 103, 2, '#', '', 'F', '0', '1', 'system:config:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1032, 'Config Update', 103, 3, '#', '', 'F', '0', '1', 'system:config:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1033, 'Config Delete', 103, 4, '#', '', 'F', '0', '1', 'system:config:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1034, 'Config Export', 103, 5, '#', '', 'F', '0', '1', 'system:config:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Dict Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(104, 'Dict Mgmt', 1, 5, '/system/dict', '', 'C', '0', '1', 'system:dict:view', 'fa fa-bookmark-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1040, 'Dict Query', 104, 1, '#', '', 'F', '0', '1', 'system:dict:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1041, 'Dict Add', 104, 2, '#', '', 'F', '0', '1', 'system:dict:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1042, 'Dict Update', 104, 3, '#', '', 'F', '0', '1', 'system:dict:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1043, 'Dict Delete', 104, 4, '#', '', 'F', '0', '1', 'system:dict:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1044, 'Dict Export', 104, 5, '#', '', 'F', '0', '1', 'system:dict:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Post Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(105, 'Post Mgmt', 1, 6, '/system/post', '', 'C', '0', '1', 'system:post:view', 'fa fa-address-card-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1050, 'Post Query', 105, 1, '#', '', 'F', '0', '1', 'system:post:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1051, 'Post Add', 105, 2, '#', '', 'F', '0', '1', 'system:post:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1052, 'Post Update', 105, 3, '#', '', 'F', '0', '1', 'system:post:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1053, 'Post Delete', 105, 4, '#', '', 'F', '0', '1', 'system:post:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1054, 'Post Export', 105, 5, '#', '', 'F', '0', '1', 'system:post:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Login Info (Monitor)
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(106, 'Login Info', 1, 7, '/system/logininfor', '', 'C', '0', '1', 'system:logininfor:view', 'fa fa-file-image-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1060, 'Login Info Query', 106, 1, '#', '', 'F', '0', '1', 'system:logininfor:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1061, 'Login Info Delete', 106, 2, '#', '', 'F', '0', '1', 'system:logininfor:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1062, 'Login Info Clean', 106, 3, '#', '', 'F', '0', '1', 'system:logininfor:clean', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1063, 'Login Info Export', 106, 4, '#', '', 'F', '0', '1', 'system:logininfor:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Operation Log (Monitor)
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(107, 'Oper Log', 1, 8, '/system/operlog', '', 'C', '0', '1', 'system:operlog:view', 'fa fa-file-image-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1070, 'Oper Log Query', 107, 1, '#', '', 'F', '0', '1', 'system:operlog:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1071, 'Oper Log Delete', 107, 2, '#', '', 'F', '0', '1', 'system:operlog:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1072, 'Oper Log Clean', 107, 3, '#', '', 'F', '0', '1', 'system:operlog:clean', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1073, 'Oper Log Export', 107, 4, '#', '', 'F', '0', '1', 'system:operlog:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- Notice Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(108, 'Notice Mgmt', 1, 9, '/system/notice', '', 'C', '0', '1', 'system:notice:view', 'fa fa-bullhorn', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1080, 'Notice Query', 108, 1, '#', '', 'F', '0', '1', 'system:notice:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1081, 'Notice Add', 108, 2, '#', '', 'F', '0', '1', 'system:notice:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1082, 'Notice Update', 108, 3, '#', '', 'F', '0', '1', 'system:notice:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(1083, 'Notice Delete', 108, 4, '#', '', 'F', '0', '1', 'system:notice:remove', '#', '0', 'admin', current_timestamp, '', null, '');

-- ==================== QUARTZ MODULE MENUS ====================
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2, 'Job Mgmt', 0, 2, '#', '', 'M', '0', '1', '', 'fa fa-tasks', '0', 'admin', current_timestamp, '', null, 'Scheduled Job Management');

-- Job Management
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(200, 'Job List', 2, 1, '/system/job', '', 'C', '0', '1', 'system:job:view', 'fa fa-clock-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2000, 'Job Query', 200, 1, '#', '', 'F', '0', '1', 'system:job:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2001, 'Job Add', 200, 2, '#', '', 'F', '0', '1', 'system:job:add', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2002, 'Job Update', 200, 3, '#', '', 'F', '0', '1', 'system:job:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2003, 'Job Delete', 200, 4, '#', '', 'F', '0', '1', 'system:job:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2004, 'Job Change Status', 200, 5, '#', '', 'F', '0', '1', 'system:job:changeStatus', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2005, 'Job Run', 200, 6, '#', '', 'F', '0', '1', 'system:job:run', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2006, 'Job Pause', 200, 7, '#', '', 'F', '0', '1', 'system:job:pause', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2007, 'Job Resume', 200, 8, '#', '', 'F', '0', '1', 'system:job:resume', '#', '0', 'admin', current_timestamp, '', null, '');

-- Job Log
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(201, 'Job Log', 2, 2, '/system/jobLog', '', 'C', '0', '1', 'system:jobLog:view', 'fa fa-file-text-o', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2010, 'Job Log Query', 201, 1, '#', '', 'F', '0', '1', 'system:jobLog:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2011, 'Job Log Delete', 201, 2, '#', '', 'F', '0', '1', 'system:jobLog:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2012, 'Job Log Clean', 201, 3, '#', '', 'F', '0', '1', 'system:jobLog:clean', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(2013, 'Job Log Export', 201, 4, '#', '', 'F', '0', '1', 'system:jobLog:export', '#', '0', 'admin', current_timestamp, '', null, '');

-- ==================== GENERATOR MODULE MENUS ====================
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(3, 'Generator', 0, 3, '#', '', 'M', '0', '1', '', 'fa fa-code', '0', 'admin', current_timestamp, '', null, 'Code Generator');

-- Code Generation
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(300, 'Code Gen', 3, 1, '/tool/gen', '', 'C', '0', '1', 'tool:gen:view', 'fa fa-code', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(3000, 'Code Gen Query', 300, 1, '#', '', 'F', '0', '1', 'tool:gen:list', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(3001, 'Code Gen Update', 300, 2, '#', '', 'F', '0', '1', 'tool:gen:edit', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(3002, 'Code Gen Delete', 300, 3, '#', '', 'F', '0', '1', 'tool:gen:remove', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(3003, 'Code Gen Import', 300, 4, '#', '', 'F', '0', '1', 'tool:gen:import', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(3004, 'Code Gen Preview', 300, 5, '#', '', 'F', '0', '1', 'tool:gen:preview', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(3005, 'Code Gen Download', 300, 6, '#', '', 'F', '0', '1', 'tool:gen:download', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(3006, 'Code Gen Sync', 300, 7, '#', '', 'F', '0', '1', 'tool:gen:synchDb', '#', '0', 'admin', current_timestamp, '', null, '');
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
values(3007, 'Code Gen Batch', 300, 8, '#', '', 'F', '0', '1', 'tool:gen:batchGen', '#', '0', 'admin', current_timestamp, '', null, '');

-- ==================== ROLE-MENU ASSIGNMENTS ====================
-- Super Admin gets all menus (parent menus + all permissions)
insert into sys_role_menu (role_id, menu_id) values (1, 1);
insert into sys_role_menu (role_id, menu_id) values (1, 100);
insert into sys_role_menu (role_id, menu_id) values (1, 1000);
insert into sys_role_menu (role_id, menu_id) values (1, 1001);
insert into sys_role_menu (role_id, menu_id) values (1, 1002);
insert into sys_role_menu (role_id, menu_id) values (1, 1003);
insert into sys_role_menu (role_id, menu_id) values (1, 1004);
insert into sys_role_menu (role_id, menu_id) values (1, 1005);
insert into sys_role_menu (role_id, menu_id) values (1, 1006);
insert into sys_role_menu (role_id, menu_id) values (1, 101);
insert into sys_role_menu (role_id, menu_id) values (1, 1010);
insert into sys_role_menu (role_id, menu_id) values (1, 1011);
insert into sys_role_menu (role_id, menu_id) values (1, 1012);
insert into sys_role_menu (role_id, menu_id) values (1, 1013);
insert into sys_role_menu (role_id, menu_id) values (1, 1014);
insert into sys_role_menu (role_id, menu_id) values (1, 102);
insert into sys_role_menu (role_id, menu_id) values (1, 1020);
insert into sys_role_menu (role_id, menu_id) values (1, 1021);
insert into sys_role_menu (role_id, menu_id) values (1, 1022);
insert into sys_role_menu (role_id, menu_id) values (1, 1023);
insert into sys_role_menu (role_id, menu_id) values (1, 103);
insert into sys_role_menu (role_id, menu_id) values (1, 1030);
insert into sys_role_menu (role_id, menu_id) values (1, 1031);
insert into sys_role_menu (role_id, menu_id) values (1, 1032);
insert into sys_role_menu (role_id, menu_id) values (1, 1033);
insert into sys_role_menu (role_id, menu_id) values (1, 1034);
insert into sys_role_menu (role_id, menu_id) values (1, 104);
insert into sys_role_menu (role_id, menu_id) values (1, 1040);
insert into sys_role_menu (role_id, menu_id) values (1, 1041);
insert into sys_role_menu (role_id, menu_id) values (1, 1042);
insert into sys_role_menu (role_id, menu_id) values (1, 1043);
insert into sys_role_menu (role_id, menu_id) values (1, 1044);
insert into sys_role_menu (role_id, menu_id) values (1, 105);
insert into sys_role_menu (role_id, menu_id) values (1, 1050);
insert into sys_role_menu (role_id, menu_id) values (1, 1051);
insert into sys_role_menu (role_id, menu_id) values (1, 1052);
insert into sys_role_menu (role_id, menu_id) values (1, 1053);
insert into sys_role_menu (role_id, menu_id) values (1, 1054);
insert into sys_role_menu (role_id, menu_id) values (1, 106);
insert into sys_role_menu (role_id, menu_id) values (1, 1060);
insert into sys_role_menu (role_id, menu_id) values (1, 1061);
insert into sys_role_menu (role_id, menu_id) values (1, 1062);
insert into sys_role_menu (role_id, menu_id) values (1, 1063);
insert into sys_role_menu (role_id, menu_id) values (1, 1064);
insert into sys_role_menu (role_id, menu_id) values (1, 107);
insert into sys_role_menu (role_id, menu_id) values (1, 1070);
insert into sys_role_menu (role_id, menu_id) values (1, 1071);
insert into sys_role_menu (role_id, menu_id) values (1, 1072);
insert into sys_role_menu (role_id, menu_id) values (1, 1073);
insert into sys_role_menu (role_id, menu_id) values (1, 1074);
insert into sys_role_menu (role_id, menu_id) values (1, 108);
insert into sys_role_menu (role_id, menu_id) values (1, 1080);
insert into sys_role_menu (role_id, menu_id) values (1, 1081);
insert into sys_role_menu (role_id, menu_id) values (1, 1082);
insert into sys_role_menu (role_id, menu_id) values (1, 1083);
insert into sys_role_menu (role_id, menu_id) values (1, 1084);
insert into sys_role_menu (role_id, menu_id) values (1, 2);
insert into sys_role_menu (role_id, menu_id) values (1, 200);
insert into sys_role_menu (role_id, menu_id) values (1, 201);
insert into sys_role_menu (role_id, menu_id) values (1, 3);
insert into sys_role_menu (role_id, menu_id) values (1, 300);

-- Common User gets limited access
insert into sys_role_menu (role_id, menu_id) values (2, 1);
insert into sys_role_menu (role_id, menu_id) values (2, 100);
insert into sys_role_menu (role_id, menu_id) values (2, 1000);
insert into sys_role_menu (role_id, menu_id) values (2, 106);
insert into sys_role_menu (role_id, menu_id) values (2, 1060);
insert into sys_role_menu (role_id, menu_id) values (2, 107);
insert into sys_role_menu (role_id, menu_id) values (2, 1070);

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

-- 1. JOB VERSIONING TABLE
CREATE TABLE IF NOT EXISTS sys_job_version (
    version_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    job_name VARCHAR(64),
    job_group VARCHAR(64),
    invoke_target VARCHAR(500),
    cron_expression VARCHAR(255),
    misfire_policy VARCHAR(20),
    concurrent VARCHAR(1),
    status VARCHAR(1),
    max_retry_count INT,
    retry_interval INT,
    timeout_seconds INT,
    notify_on_failure BOOLEAN,
    notification_emails VARCHAR(500),
    webhook_url VARCHAR(500),
    dependent_job_ids VARCHAR(500),
    time_zone VARCHAR(50),
    allow_holiday BOOLEAN,
    remark VARCHAR(500),
    changed_by VARCHAR(64),
    change_reason VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_job_id (job_id),
    INDEX idx_version_number (version_number)
);

-- 2. HOLIDAY CALENDAR TABLE
CREATE TABLE IF NOT EXISTS sys_holiday (
    holiday_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    holiday_name VARCHAR(100) NOT NULL,
    holiday_date DATE NOT NULL,
    holiday_type VARCHAR(1) DEFAULT '1', -- 1=National, 2=Company, 3=Optional
    is_recurring BOOLEAN DEFAULT FALSE,
    description VARCHAR(500),
    status VARCHAR(1) DEFAULT '0', -- 0=Active, 1=Inactive
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP,
    UNIQUE KEY uk_holiday_date (holiday_date)
);

-- 3. JOB ACCESS CONTROL TABLE
CREATE TABLE IF NOT EXISTS sys_job_role (
    job_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (job_id, role_id),
    INDEX idx_job_id (job_id),
    INDEX idx_role_id (role_id)
);

-- 4. JOB EXECUTION HISTORY (for Gantt/Timeline)
CREATE TABLE IF NOT EXISTS sys_job_execution (
    execution_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    job_name VARCHAR(64),
    job_group VARCHAR(64),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    status VARCHAR(1), -- 0=Success, 1=Failed, 2=Running
    trigger_type VARCHAR(20), -- CRON, MANUAL, DEPENDENT
    retry_count INT DEFAULT 0,
    error_message VARCHAR(2000),
    INDEX idx_job_id (job_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (status)
);

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

-- =====================================================
-- ADD NEW MENU ITEMS FOR PENDING FEATURES
-- Add under parent ID 2 (Job Mgmt - type 'M') not 200 (type 'C')
-- =====================================================

-- Job Calendar menu (under Job Mgmt parent_id=2)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(2020, 'Job Calendar', 2, 20, '/system/job-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', current_timestamp, '', null, 'Job Calendar View');

-- Holiday Calendar menu
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(2021, 'Holiday Calendar', 2, 21, '/system/holiday-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', current_timestamp, '', null, 'Holiday Calendar Management');

-- Live Job Logs menu
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(2022, 'Live Logs', 2, 22, '/system/job-logs', '', 'C', '0', '1', 'system:job:list', 'fa fa-terminal', '0', 'admin', current_timestamp, '', null, 'Real-time Job Logs');

-- Email Templates menu
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(2023, 'Email Templates', 2, 23, '/system/email-templates', '', 'C', '0', '1', 'system:job:template', 'fa fa-envelope', '0', 'admin', current_timestamp, '', null, 'Email Template Management');

-- Add menu permissions for admin role (role_id=1)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2020);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2021);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2022);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2023);

-- Email Config menu (under System Config parent_id=103)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, update_by, update_time, remark)
VALUES(1034, 'Email Config', 103, 4, '/system/email-config', '', 'C', '0', '1', 'system:config:email', 'fa fa-envelope', '0', 'admin', current_timestamp, '', null, 'Email Server Configuration');

-- Add menu permissions for admin role
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1034);
  
-- Role-Menu permissions for new menus  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2020);  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2021);  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2022);  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2023);  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 1034);  
  
  
-- ====================================================  
-- NEW MENUS - Job Scheduling Module  
-- ====================================================  
  
-- Job Calendar  
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)  
VALUES (2020, 'Job Calendar', 2, 20, '/system/job-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', CURRENT_TIMESTAMP, 'Job Calendar View');  
  
-- Holiday Calendar  
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)  
VALUES (2021, 'Holiday Calendar', 2, 21, '/system/holiday-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', CURRENT_TIMESTAMP, 'Holiday Calendar Management');  
  
-- Live Logs  
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)  
VALUES (2022, 'Live Logs', 2, 22, '/system/job-logs', '', 'C', '0', '1', 'system:job:list', 'fa fa-terminal', '0', 'admin', CURRENT_TIMESTAMP, 'Real-time Job Logs');  
  
-- Email Templates  
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)  
VALUES (2023, 'Email Templates', 2, 23, '/system/email-templates', '', 'C', '0', '1', 'system:job:template', 'fa fa-envelope', '0', 'admin', CURRENT_TIMESTAMP, 'Email Template Management');  
  
-- Email Config  
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)  
VALUES (1034, 'Email Config', 103, 4, '/system/email-config', '', 'C', '0', '1', 'system:config:email', 'fa fa-envelope', '0', 'admin', CURRENT_TIMESTAMP, 'Email Server Configuration');  
  
-- Role-Menu Permissions  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2020);  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2021);  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2022);  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2023);  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 1034);  
  
  
-- Cache Management Menu  
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)  
VALUES (1035, 'Cache Management', 103, 5, '/system/cache', '', 'C', '0', '1', 'system:config:cache', 'fa fa-database', '0', 'admin', CURRENT_TIMESTAMP, 'Cache Management');  
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 1035);  
