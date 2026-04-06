-- Report Module - Complete Schema Migration
-- Run this ONCE to create all report module tables
-- This combines report-designer, report-versioning, and report-enhancement migrations

-- ========================================
-- 1. Report Template Table (Report Designer)
-- ========================================
CREATE TABLE IF NOT EXISTS sys_report_template (
    template_id         BIGINT          NOT NULL,
    template_name       VARCHAR(200)    NOT NULL,
    template_key        VARCHAR(100)    NOT NULL,
    description         VARCHAR(500),
    datasource_key      VARCHAR(100)    NOT NULL,
    report_mode         VARCHAR(20)     DEFAULT 'SQL',
    sql_content         CLOB,
    tables_config       CLOB,
    columns_config      CLOB,
    filters_config      CLOB,
    group_by_config     CLOB,
    order_by_config     CLOB,
    charts_config       CLOB,
    layout_config       CLOB,
    output_format       VARCHAR(20)     DEFAULT 'EXCEL',
    status              VARCHAR(1)      DEFAULT '0',
    version             INT             DEFAULT 1,
    parent_template_id  BIGINT,
    change_log          VARCHAR(500),
    create_by           VARCHAR(64),
    create_time         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_by           VARCHAR(64),
    update_time         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    remark              VARCHAR(500),
    PRIMARY KEY (template_id),
    UNIQUE (template_key),
    FOREIGN KEY (parent_template_id) REFERENCES sys_report_template(template_id)
);

CREATE SEQUENCE IF NOT EXISTS sys_report_template_seq START WITH 100 INCREMENT BY 1;

-- Indexes for report templates
CREATE INDEX IF NOT EXISTS idx_report_template_key ON sys_report_template(template_key);
CREATE INDEX IF NOT EXISTS idx_report_template_key_version ON sys_report_template(template_key, version DESC);
CREATE INDEX IF NOT EXISTS idx_report_template_status ON sys_report_template(status);
CREATE INDEX IF NOT EXISTS idx_report_template_parent ON sys_report_template(parent_template_id);

-- ========================================
-- 2. Report Email Config Table
-- ========================================
CREATE TABLE IF NOT EXISTS sys_report_email_config (
    config_id           BIGINT          NOT NULL,
    report_id           BIGINT          NOT NULL,
    template_id         BIGINT,
    recipient_emails    CLOB,
    cc_emails           CLOB,
    email_subject       VARCHAR(300),
    email_body          CLOB,
    attachment_format   VARCHAR(20)     DEFAULT 'EXCEL',
    send_on_success     VARCHAR(1)      DEFAULT '1',
    send_on_failure     VARCHAR(1)      DEFAULT '1',
    status              VARCHAR(1)      DEFAULT '0',
    create_time         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (config_id),
    FOREIGN KEY (report_id) REFERENCES sys_report(report_id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES sys_report_template(template_id) ON DELETE SET NULL
);

CREATE SEQUENCE IF NOT EXISTS sys_report_email_config_seq START WITH 100 INCREMENT BY 1;

-- ========================================
-- 3. Datasource Metadata Cache Table
-- ========================================
CREATE TABLE IF NOT EXISTS sys_datasource_meta (
    meta_id             BIGINT          NOT NULL,
    datasource_id       BIGINT          NOT NULL,
    table_name          VARCHAR(200)    NOT NULL,
    table_comment       VARCHAR(500),
    columns_meta        CLOB,
    last_sync_time      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (meta_id),
    FOREIGN KEY (datasource_id) REFERENCES sys_datasource(datasource_id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS sys_datasource_meta_seq START WITH 100 INCREMENT BY 1;

-- ========================================
-- 4. Report Job Execution Log Table
-- ========================================
CREATE TABLE IF NOT EXISTS sys_report_job_log (
    log_id              BIGINT          NOT NULL,
    report_id           BIGINT          NOT NULL,
    job_name            VARCHAR(100),
    execution_time      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    status              VARCHAR(1)      DEFAULT '0',
    rows_generated      INT,
    error_message       VARCHAR(1000),
    PRIMARY KEY (log_id)
);

CREATE SEQUENCE IF NOT EXISTS sys_report_job_log_seq START WITH 100 INCREMENT BY 1;

-- ========================================
-- 5. Link sys_report to templates
-- ========================================
ALTER TABLE sys_report ADD COLUMN IF NOT EXISTS template_id BIGINT;
ALTER TABLE sys_report ADD CONSTRAINT IF NOT EXISTS fk_report_template 
    FOREIGN KEY (template_id) REFERENCES sys_report_template(template_id);

-- ========================================
-- 6. Menu Entries for Report Designer
-- ========================================
-- Report Designer menu (under Report Management, parent_id=5000)
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
VALUES (5007, 'Report Designer', 5000, 2, '/system/report-designer', '', 'C', '0', '1', 'system:report:designer', 'fa fa-wrench', '0', 'admin', CURRENT_TIMESTAMP, 'Visual report designer with drag-and-drop');

-- Permissions for report designer
MERGE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, status, create_by, create_time, remark)
VALUES 
(5009, 'Template Query', 5007, 1, '#', '', 'F', '0', '1', 'system:report:template:query', '#', '0', 'admin', CURRENT_TIMESTAMP, ''),
(5010, 'Template Add', 5007, 2, '#', '', 'F', '0', '1', 'system:report:template:add', '#', '0', 'admin', CURRENT_TIMESTAMP, ''),
(5011, 'Template Edit', 5007, 3, '#', '', 'F', '0', '1', 'system:report:template:edit', '#', '0', 'admin', CURRENT_TIMESTAMP, ''),
(5012, 'Template Delete', 5007, 4, '#', '', 'F', '0', '1', 'system:report:template:remove', '#', '0', 'admin', CURRENT_TIMESTAMP, ''),
(5013, 'Template Execute', 5007, 5, '#', '', 'F', '0', '1', 'system:report:template:execute', '#', '0', 'admin', CURRENT_TIMESTAMP, '');

-- Add role-menu permissions for admin role (role_id=1)
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5007);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5009);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5010);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5011);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5012);
MERGE INTO sys_role_menu (role_id, menu_id) VALUES (1, 5013);
