-- Report Template Versioning Migration
-- Adds version tracking and status management for report templates

-- Add version and status columns
ALTER TABLE sys_report_template ADD COLUMN IF NOT EXISTS version INT DEFAULT 1;
ALTER TABLE sys_report_template ADD COLUMN IF NOT EXISTS parent_template_id BIGINT;
ALTER TABLE sys_report_template ADD COLUMN IF NOT EXISTS change_log VARCHAR(500);

-- Add index for faster version lookups
CREATE INDEX IF NOT EXISTS idx_report_template_key_version ON sys_report_template(template_key, version DESC);
CREATE INDEX IF NOT EXISTS idx_report_template_status ON sys_report_template(status);
