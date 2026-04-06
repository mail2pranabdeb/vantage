-- Report Template Enhancement Migration
-- Run this AFTER the report-designer-migration.sql
-- Adds template_id linking and job execution logging

-- Add template_id to sys_report for linking to designer templates
ALTER TABLE sys_report ADD COLUMN IF NOT EXISTS template_id BIGINT;
ALTER TABLE sys_report ADD CONSTRAINT IF NOT EXISTS fk_report_template 
    FOREIGN KEY (template_id) REFERENCES sys_report_template(template_id);

-- Create job execution log table for reports (optional)
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
