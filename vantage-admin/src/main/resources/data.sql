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
values(200, 'Job Mgmt', 2, 1, '/system/job', '', 'C', '0', '1', 'system:job:view', 'fa fa-clock-o', '0', 'admin', current_timestamp, '', null, '');
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
-- Super Admin gets all menus
insert into sys_role_menu (role_id, menu_id) values (1, 1);
insert into sys_role_menu (role_id, menu_id) values (1, 100);
insert into sys_role_menu (role_id, menu_id) values (1, 101);
insert into sys_role_menu (role_id, menu_id) values (1, 102);
insert into sys_role_menu (role_id, menu_id) values (1, 103);
insert into sys_role_menu (role_id, menu_id) values (1, 104);
insert into sys_role_menu (role_id, menu_id) values (1, 105);
insert into sys_role_menu (role_id, menu_id) values (1, 106);
insert into sys_role_menu (role_id, menu_id) values (1, 107);
insert into sys_role_menu (role_id, menu_id) values (1, 108);
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
