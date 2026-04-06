-- Report Template Versioning Migration
-- Run this AFTER the report-designer-migration.sql
-- Adds version tracking and status management for report templates

-- Add version column (defaults to 1 for existing records)
ALTER TABLE sys_report_template ADD COLUMN IF NOT EXISTS version INT DEFAULT 1;

-- Add parent template ID for version lineage tracking
ALTER TABLE sys_report_template ADD COLUMN IF NOT EXISTS parent_template_id BIGINT;
ALTER TABLE sys_report_template ADD CONSTRAINT IF NOT EXISTS fk_report_template_parent 
    FOREIGN KEY (parent_template_id) REFERENCES sys_report_template(template_id);

-- Add change log field for documenting what changed in each version
ALTER TABLE sys_report_template ADD COLUMN IF NOT EXISTS change_log VARCHAR(500);

-- Add indexes for faster version lookups
CREATE INDEX IF NOT EXISTS idx_report_template_key_version 
    ON sys_report_template(template_key, version DESC);
CREATE INDEX IF NOT EXISTS idx_report_template_status 
    ON sys_report_template(status);
CREATE INDEX IF NOT EXISTS idx_report_template_parent 
    ON sys_report_template(parent_template_id);
