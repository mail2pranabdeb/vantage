-- Report Activation and Job Integration Migration

-- 1. Add job_type and report_id to sys_job
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS job_type VARCHAR(20) DEFAULT 'BEAN';
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS report_id BIGINT;

-- 2. Allow invoke_target to be NULL in sys_job and sys_job_log (required for REPORT type jobs)
ALTER TABLE sys_job ALTER COLUMN invoke_target SET NULL;
ALTER TABLE sys_job_log ALTER COLUMN invoke_target SET NULL;

-- 3. Ensure version and status columns exist in sys_report_template
ALTER TABLE sys_report_template ADD COLUMN IF NOT EXISTS version INT DEFAULT 1;
ALTER TABLE sys_report_template ADD COLUMN IF NOT EXISTS status VARCHAR(1) DEFAULT '1';

-- 4. Set all existing reports to Active (status='0') and Version 1
UPDATE sys_report_template SET status = '0', version = 1 WHERE status IS NULL OR version IS NULL;
