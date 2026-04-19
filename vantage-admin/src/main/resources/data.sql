-- =====================================================
-- VANTAGE ADMIN - Initial Data
-- Database Agnostic (auto-generated IDs)
-- =====================================================

-- 1. Security & Core User Data
INSERT INTO sys_user (login_name, user_name, user_type, email, phonenumber, sex, avatar, password, salt, status, del_flag, login_ip, create_by, create_time, remark)
VALUES('admin', 'Administrator', '00', 'admin@vantage.vip', '15888888888', '1', '', '$2a$10$CUmdVx1.RaVkRGu.pISr3.8/iPWinkuYQb.Jk7G2b4FVnk7qbUJsa', '', '0', '0', '127.0.0.1', 'admin', current_timestamp, 'Super Administrator');

INSERT INTO sys_user (login_name, user_name, user_type, email, phonenumber, sex, avatar, password, salt, status, del_flag, login_ip, create_by, create_time, remark)
VALUES('prihan', 'Prihan', '00', 'prihan@qq.com', '15666666666', '1', '', '$2a$10$CUmdVx1.RaVkRGu.pISr3.8/iPWinkuYQb.Jk7G2b4FVnk7qbUJsa', '', '0', '0', '127.0.0.1', 'admin', current_timestamp, 'Tester');

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark)
VALUES('Super Admin', 'admin', 1, '1', '0', '0', 'admin', current_timestamp, 'Super Administrator');

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark)
VALUES('Common User', 'common', 2, '2', '0', '0', 'admin', current_timestamp, 'Common User Role');

INSERT INTO sys_user_role (user_id, role_id) 
SELECT u.user_id, r.role_id FROM sys_user u, sys_role r WHERE u.login_name = 'admin' AND r.role_key = 'admin';

INSERT INTO sys_user_role (user_id, role_id) 
SELECT u.user_id, r.role_id FROM sys_user u, sys_role r WHERE u.login_name = 'prihan' AND r.role_key = 'common';

-- 2. System Configuration & SMTP Settings
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('mail host', 'mail.host', 'smtp.gmail.com', 'Y', 'admin', CURRENT_TIMESTAMP, 'System setting');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('mail port', 'mail.port', '587', 'Y', 'admin', CURRENT_TIMESTAMP, 'System setting');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('mail username', 'mail.username', 'mailservicekedb@gmail.com', 'Y', 'admin', CURRENT_TIMESTAMP, 'System setting');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('mail password', 'mail.password', 'ndomzzwgpyvdsomb', 'Y', 'admin', CURRENT_TIMESTAMP, 'System setting');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('mail fromEmail', 'mail.fromEmail', 'mailservicekedb@gmail.com', 'Y', 'admin', CURRENT_TIMESTAMP, 'System setting');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('mail fromName', 'mail.fromName', 'Vantage Admin', 'Y', 'admin', CURRENT_TIMESTAMP, 'System setting');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('mail enableAuth', 'mail.enableAuth', 'true', 'Y', 'admin', CURRENT_TIMESTAMP, 'System setting');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('mail enableTls', 'mail.enableTls', 'true', 'Y', 'admin', CURRENT_TIMESTAMP, 'System setting');

-- 3. Core Menus
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
VALUES('System Management', 0, 1, '#', '', 'M', '0', '1', '', 'fa fa-gear', '0', 'admin', current_timestamp, 'System Management');

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
VALUES('Job Management', 0, 2, '#', '', 'M', '0', '1', '', 'fa fa-tasks', '0', 'admin', current_timestamp, 'Scheduled Job Management');

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
VALUES('Code Gen Management', 0, 3, '#', '', 'M', '0', '1', '', 'fa fa-code', '0', 'admin', current_timestamp, 'Code Generator');

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
VALUES('Report Management', 0, 4, '#', '', 'M', '0', '1', '', 'fa fa-file-text', '0', 'admin', CURRENT_TIMESTAMP, 'Report Management');

-- System Management children (parent_id from System Management)
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'User Mgmt', m.menu_id, 1, '/system/user', '', 'C', '0', '1', 'system:user:list,system:user:query,system:user:add,system:user:edit,system:user:remove', 'fa fa-user-o', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'System Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Role Mgmt', m.menu_id, 2, '/system/role', '', 'C', '0', '1', 'system:role:list,system:role:query,system:role:add,system:role:edit,system:role:remove', 'fa fa-user-secret', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'System Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Menu Mgmt', m.menu_id, 3, '/system/menu', '', 'C', '0', '1', 'system:menu:list,system:menu:query,system:menu:add,system:menu:edit,system:menu:remove', 'fa fa-th-list', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'System Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Config Mgmt', m.menu_id, 4, '/system/config', '', 'C', '0', '1', 'system:config:list,system:config:query,system:config:add,system:config:edit,system:config:remove', 'fa fa-sun-o', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'System Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Dict Mgmt', m.menu_id, 5, '/system/dict', '', 'C', '0', '1', 'system:dict:list,system:dict:query,system:dict:add,system:dict:edit,system:dict:remove', 'fa fa-bookmark-o', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'System Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Login Info', m.menu_id, 7, '/system/logininfor', '', 'C', '0', '1', 'system:logininfor:list,system:logininfor:query', 'fa fa-file-image-o', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'System Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Oper Log', m.menu_id, 8, '/system/operlog', '', 'C', '0', '1', 'system:operlog:list,system:operlog:query', 'fa fa-file-image-o', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'System Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Notice Mgmt', m.menu_id, 9, '/system/notice', '', 'C', '0', '1', 'system:notice:list,system:notice:query,system:notice:add,system:notice:edit,system:notice:remove', 'fa fa-bullhorn', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'System Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Cache Mgmt', m.menu_id, 5, '/system/cache', '', 'C', '0', '1', 'system:config:cache', 'fa fa-database', '0', 'admin', CURRENT_TIMESTAMP, 'Cache Management' FROM sys_menu m WHERE m.menu_name = 'System Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Multi-Datasource', m.menu_id, 10, '/system/datasource', '', 'C', '0', '1', 'system:datasource:list,system:datasource:query,system:datasource:add,system:datasource:edit,system:datasource:remove,system:datasource:test', 'fa fa-database', '0', 'admin', CURRENT_TIMESTAMP, 'Multi-Datasource Management' FROM sys_menu m WHERE m.menu_name = 'System Management';

-- Job Management children (parent_id from Job Management)
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Job List', m.menu_id, 1, '/system/job', '', 'C', '0', '1', 'system:job:list,system:job:query,system:job:add,system:job:edit,system:job:remove', 'fa fa-clock-o', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'Job Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Job Log', m.menu_id, 2, '/system/jobLog', '', 'C', '0', '1', 'system:jobLog:list,system:jobLog:query', 'fa fa-file-text-o', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'Job Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Job Calendar', m.menu_id, 20, '/system/job-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', current_timestamp, 'Job Calendar View' FROM sys_menu m WHERE m.menu_name = 'Job Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Holiday Calendar', m.menu_id, 21, '/system/holiday-calendar', '', 'C', '0', '1', 'system:job:calendar', 'fa fa-calendar', '0', 'admin', current_timestamp, 'Holiday Calendar Management' FROM sys_menu m WHERE m.menu_name = 'Job Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Live Logs', m.menu_id, 22, '/system/job-logs', '', 'C', '0', '1', 'system:job:list', 'fa fa-terminal', '0', 'admin', current_timestamp, 'Real-time Job Logs' FROM sys_menu m WHERE m.menu_name = 'Job Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Email Templates', m.menu_id, 23, '/system/email-templates', '', 'C', '0', '1', 'system:job:template', 'fa fa-envelope', '0', 'admin', current_timestamp, 'Email Template Management' FROM sys_menu m WHERE m.menu_name = 'Job Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Script Jobs', m.menu_id, 6, '/system/script-job', '', 'C', '0', '1', 'system:scriptJob:list', 'fa fa-file-code', '0', 'admin', CURRENT_TIMESTAMP, 'GLUE-like Script Jobs' FROM sys_menu m WHERE m.menu_name = 'Job Management';

-- Report Management children (parent_id from Report Management)
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Report List', m.menu_id, 1, '/system/report', '', 'C', '0', '1', 'system:report:list,system:report:query,system:report:add,system:report:edit,system:report:remove,system:report:execute', 'fa fa-list', '0', 'admin', CURRENT_TIMESTAMP, 'Report List' FROM sys_menu m WHERE m.menu_name = 'Report Management';

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Report Designer', m.menu_id, 2, '/system/report-designer', '', 'C', '0', '1', 'system:report:template:query,system:report:template:add,system:report:template:edit,system:report:template:remove,system:report:template:execute', 'fa fa-wrench', '0', 'admin', CURRENT_TIMESTAMP, 'Visual report designer' FROM sys_menu m WHERE m.menu_name = 'Report Management';

-- Code Gen Management children (parent_id from Code Gen Management)
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
SELECT 'Code Gen', m.menu_id, 1, '/tool/gen', '', 'C', '0', '1', 'tool:gen:list,tool:gen:preview,tool:gen:code,tool:gen:download', 'fa fa-code', '0', 'admin', current_timestamp, '' FROM sys_menu m WHERE m.menu_name = 'Code Gen Management';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id FROM sys_role r, sys_menu m WHERE r.role_key = 'admin';

-- 4. Dictionary Settings
INSERT INTO sys_dict_type (dict_type, dict_name, status, create_by, create_time, remark)
VALUES ('sys_normal_disable', 'Normal/Disabled Status', '0', 'admin', current_timestamp, 'System status options');

INSERT INTO sys_dict_type (dict_type, dict_name, status, create_by, create_time, remark)
VALUES ('sys_user_sex', 'User Gender', '0', 'admin', current_timestamp, 'Gender options');

INSERT INTO sys_dict_type (dict_type, dict_name, status, create_by, create_time, remark)
VALUES ('sys_yes_no', 'Yes/No Options', '0', 'admin', current_timestamp, 'Boolean options');

INSERT INTO sys_dict_type (dict_type, dict_name, status, create_by, create_time, remark)
VALUES ('sys_job_group', 'Job Groups', '0', 'admin', current_timestamp, 'Quartz job groups');

INSERT INTO sys_dict_type (dict_type, dict_name, status, create_by, create_time, remark)
VALUES ('sys_report_email_group', 'Report Email Group', '0', 'admin', current_timestamp, 'Email recipient groups');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark)
VALUES (1, 'Normal', '0', 'sys_normal_disable', '0', 'admin', current_timestamp, 'Normal status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark)
VALUES (2, 'Disabled', '1', 'sys_normal_disable', '0', 'admin', current_timestamp, 'Disabled status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark)
VALUES (1, 'Male', '1', 'sys_user_sex', '0', 'admin', current_timestamp, 'Male gender');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark)
VALUES (2, 'Female', '2', 'sys_user_sex', '0', 'admin', current_timestamp, 'Female gender');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark)
VALUES (1, 'Yes', 'Y', 'sys_yes_no', '0', 'admin', current_timestamp, 'Yes option');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark)
VALUES (2, 'No', 'N', 'sys_yes_no', '0', 'admin', current_timestamp, 'No option');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark)
VALUES (1, 'Default', 'DEFAULT', 'sys_job_group', '0', 'admin', current_timestamp, 'Default group');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark)
VALUES (2, 'System', 'SYSTEM', 'sys_job_group', '0', 'admin', current_timestamp, 'System group');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark)
VALUES (1, 'Management Team', 'mail2pranabdeb@gmail.com,mailservicekedb@gmail.com', 'sys_report_email_group', '0', 'admin', current_timestamp, 'Default management email list');

-- 5. Holidays
INSERT INTO sys_holiday (holiday_name, holiday_date, holiday_type, description, status)
VALUES('New Years Day', '2026-01-01', 1, 'National Holiday', '0');

INSERT INTO sys_holiday (holiday_name, holiday_date, holiday_type, description, status)
VALUES('Christmas Day', '2026-12-25', 1, 'National Holiday', '0');

-- 6. Datasources
INSERT INTO sys_datasource (datasource_name, datasource_key, db_type, url, username, password, driver_class, status, create_time)
VALUES('Primary Database', 'primary', 'H2', 'jdbc:h2:file:D:/Projects/vantage-master-opencode/data/vantage', 'sa', 'vantage123', 'org.h2.Driver', '0', CURRENT_TIMESTAMP);

INSERT INTO sys_datasource (datasource_name, datasource_key, db_type, url, username, password, driver_class, status, create_time)
VALUES('MySQL Demo', 'MYSQL', 'MySQL', 'jdbc:mysql://localhost:3306/vantage', 'root', '', 'com.mysql.cj.jdbc.Driver', '1', CURRENT_TIMESTAMP);

-- 7. Email Templates
INSERT INTO sys_job_email_template (template_name, template_type, email_subject, email_body, is_default, is_active, create_time)
VALUES('Job Failure Alert', 'JOB_FAILURE', '[${appName}] Job Failed: ${jobName}', '<h2>Job Failure Notification</h2><p>A scheduled job has failed.</p><p><strong>Job:</strong> ${jobName}</p><p><strong>Error:</strong> ${message}</p>${dataTable}', true, true, CURRENT_TIMESTAMP);

INSERT INTO sys_job_email_template (template_name, template_type, email_subject, data_tables, email_body, is_default, is_active, create_time)
VALUES('Job Success Alert', 'JOB_SUCCESS', '[${appName}] Job Completed: ${jobName}', '[{"datasourceKey":"primary","query":"select * from sys_user","label":"Users:","enabled":true},{"datasourceKey":"primary","query":"select * from sys_role","label":"Roles:","enabled":true}]','<h2>Job Success Notification</h2><p>A scheduled job has completed.</p><p><strong>Duration:</strong> ${duration}ms</p>${dataTable}', true, true, CURRENT_TIMESTAMP);

-- 8. Jobs (Quartz scheduled jobs)
INSERT INTO sys_job (job_name, job_group, job_type, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark, notify_on_failure)
VALUES('Daily Backup Job', 'DEFAULT', 'BEAN', 'backupService.execute()', '0 0 2 * * ?', '3', '1', '1', 'admin', CURRENT_TIMESTAMP, 'Daily backup at 2 AM', false);

INSERT INTO sys_job (job_name, job_group, job_type, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark, notify_on_failure)
VALUES('Health Check Job', 'DEFAULT', 'BEAN', 'healthCheckService.ping()', '0 */5 * * * ?', '3', '1', '1', 'admin', CURRENT_TIMESTAMP, 'Health check every 5 minutes', false);

INSERT INTO sys_job (job_name, job_group, job_type, report_email_group, report_id, invoke_target, cron_expression, email_template_id, misfire_policy, concurrent, status, create_by, create_time, remark, notify_on_failure)
VALUES('User Report Job', 'DEFAULT', 'REPORT', '9','1' ,'', '0 */5 * * * ?', '2', '3', '1', '0', 'admin', CURRENT_TIMESTAMP, 'User Reports', false);

-- 9. Reports - sys_report_template for report-designer
INSERT INTO sys_report_template (template_name, template_key, description, datasource_key, report_mode, sql_content, output_format, status, version)
VALUES('User List Report', 'USER_LIST', 'List of all users', 'master', 'SQL', 'SELECT user_id, login_name, user_name, email FROM sys_user', 'EXCEL', '0', 1);

INSERT INTO sys_report_template (template_name, template_key, description, datasource_key, report_mode, sql_content, output_format, status, version)
VALUES('Role List Report', 'ROLE_LIST', 'Role List Report', 'master', 'SQL', 'SELECT role_id, role_name, role_key FROM sys_role', 'EXCEL', '0', 1);