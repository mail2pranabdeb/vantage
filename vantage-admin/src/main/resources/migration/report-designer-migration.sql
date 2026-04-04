-- Report Designer Migration
-- Adds support for visual report templates with drag-and-drop designer

-- Report template table for storing designer configurations
CREATE TABLE IF NOT EXISTS sys_report_template (
    template_id         BIGINT          NOT NULL,
    template_name       VARCHAR(200)    NOT NULL,
    template_key        VARCHAR(100)    NOT NULL UNIQUE,
    description         VARCHAR(500),
    datasource_key      VARCHAR(100)    NOT NULL,
    report_mode         VARCHAR(20)     DEFAULT 'SQL', -- SQL, VISUAL_BUILDER, HYBRID
    sql_content         CLOB,
    tables_config       CLOB, -- JSON: selected tables and joins
    columns_config      CLOB, -- JSON: dragged columns with formatting
    filters_config      CLOB, -- JSON: where clauses and filters
    group_by_config     CLOB, -- JSON: grouping columns
    order_by_config     CLOB, -- JSON: sorting configuration
    charts_config       CLOB, -- JSON: chart definitions (bar, line, pie)
    layout_config       CLOB, -- JSON: page layout, header, footer
    output_format       VARCHAR(20)     DEFAULT 'EXCEL', -- EXCEL, PDF, CSV, HTML, JSON
    status              VARCHAR(1)      DEFAULT '0',
    create_by           VARCHAR(64),
    create_time         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_by           VARCHAR(64),
    update_time         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    remark              VARCHAR(500),
    PRIMARY KEY (template_id)
);

CREATE SEQUENCE IF NOT EXISTS sys_report_template_seq START WITH 100 INCREMENT BY 1;

-- Email attachment config for reports
CREATE TABLE IF NOT EXISTS sys_report_email_config (
    config_id           BIGINT          NOT NULL,
    report_id           BIGINT          NOT NULL,
    template_id         BIGINT,
    recipient_emails    CLOB, -- JSON array of emails
    cc_emails           CLOB, -- JSON array of CC emails
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

-- Add template_id to sys_report for linking to designer templates
ALTER TABLE sys_report ADD COLUMN IF NOT EXISTS template_id BIGINT;
ALTER TABLE sys_report ADD CONSTRAINT fk_report_template FOREIGN KEY (template_id) REFERENCES sys_report_template(template_id);

-- Add datasource table/column metadata caching
CREATE TABLE IF NOT EXISTS sys_datasource_meta (
    meta_id             BIGINT          NOT NULL,
    datasource_id       BIGINT          NOT NULL,
    table_name          VARCHAR(200)    NOT NULL,
    table_comment       VARCHAR(500),
    columns_meta        CLOB, -- JSON: column definitions with types
    last_sync_time      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (meta_id),
    FOREIGN KEY (datasource_id) REFERENCES sys_datasource(datasource_id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS sys_datasource_meta_seq START WITH 100 INCREMENT BY 1;

-- Menu entries for Report Designer
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (5007, 'Report Designer', 5000, 4, 'report-designer', 'ReportDesigner', 1, 0, 'C', '0', '0', 'system:report:designer', 'build', 'admin', CURRENT_TIMESTAMP, '', NULL, 'Visual report designer with drag-and-drop');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (5008, 'Report Templates', 5000, 5, 'report-templates', 'ReportTemplateList', 1, 0, 'C', '0', '0', 'system:report:template', 'file-text', 'admin', CURRENT_TIMESTAMP, '', NULL, 'Manage saved report templates');

-- Permissions for report designer
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES 
(5009, 'Template Query', 5007, 1, '#', '', 1, 0, 'F', '0', '0', 'system:report:template:query', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
(5010, 'Template Add', 5007, 2, '#', '', 1, 0, 'F', '0', '0', 'system:report:template:add', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
(5011, 'Template Edit', 5007, 3, '#', '', 1, 0, 'F', '0', '0', 'system:report:template:edit', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
(5012, 'Template Delete', 5007, 4, '#', '', 1, 0, 'F', '0', '0', 'system:report:template:remove', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
(5013, 'Template Execute', 5007, 5, '#', '', 1, 0, 'F', '0', '0', 'system:report:template:execute', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
