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
