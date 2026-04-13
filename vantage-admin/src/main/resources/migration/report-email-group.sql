-- Report Job Email Group Migration

-- 1. Add Report Email Group column to sys_job
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS report_email_group VARCHAR(500);

-- 2. Add Dictionary Type for Report Email Groups
MERGE INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) 
VALUES (
    (SELECT COALESCE(MAX(dict_id), 100) + 1 FROM sys_dict_type), 
    'Report Email Group', 
    'sys_report_email_group', 
    '0', 
    'admin', 
    CURRENT_TIMESTAMP, 
    'Email recipient groups for scheduled reports'
);

-- 3. Add Sample Email Group Data
-- You can add more via the UI later (System > Dict Management)
MERGE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (
    (SELECT COALESCE(MAX(dict_code), 100) + 1 FROM sys_dict_data), 
    1, 
    'Management Team', 
    'mail2pranabdeb@gmail.com,mailservicekedb@gmail.com',
    'sys_report_email_group', 
    '', 
    '', 
    '1', 
    '0', 
    'admin', 
    CURRENT_TIMESTAMP, 
    'Default management email list'
);
