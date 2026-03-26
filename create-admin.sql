-- Create admin user
INSERT INTO sys_user (user_id, login_name, user_name, user_type, email, phonenumber, sex, password, salt, status, del_flag, create_by, create_time) 
VALUES (1, 'admin', 'Administrator', '00', 'admin@vantage.com', '13800000000', '1', '$2a$10$CUmdVx1.RaVkRGu.pISr3.8/iPWinkuYQb.Jk7G2b4FVnk7qbUJsa', '', '0', '0', 'admin', CURRENT_TIMESTAMP);

-- Create Super Admin role
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark) 
VALUES (1, 'Super Administrator', 'admin', 1, '1', '0', '0', 'admin', CURRENT_TIMESTAMP, 'Super Administrator');

-- Assign role to user
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- Verify
SELECT user_id, login_name, user_name FROM sys_user WHERE user_id = 1;
SELECT role_id, role_name FROM sys_role WHERE role_id = 1;
