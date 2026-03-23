-- Job Module Enhancement Schema Migration
-- Run this script to add new columns for enhanced job features

-- Add new columns to sys_job table
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS max_retry_count INTEGER DEFAULT 0;
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS retry_interval INTEGER DEFAULT 60;
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS timeout_seconds INTEGER DEFAULT 3600;
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS notify_on_failure BOOLEAN DEFAULT FALSE;
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS notification_emails VARCHAR(500);
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS webhook_url VARCHAR(500);
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS dependent_job_ids VARCHAR(500);
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS time_zone VARCHAR(50) DEFAULT 'UTC';
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS allow_holiday BOOLEAN DEFAULT TRUE;
ALTER TABLE sys_job ADD COLUMN IF NOT EXISTS template_name VARCHAR(64);

-- Add new columns to sys_job_log table
ALTER TABLE sys_job_log ADD COLUMN IF NOT EXISTS job_id BIGINT;
ALTER TABLE sys_job_log ADD COLUMN IF NOT EXISTS execution_duration BIGINT;
ALTER TABLE sys_job_log ADD COLUMN IF NOT EXISTS retry_count INTEGER DEFAULT 0;
ALTER TABLE sys_job_log ADD COLUMN IF NOT EXISTS create_time TIMESTAMP;

-- Create index for faster job log queries
CREATE INDEX IF NOT EXISTS idx_job_log_job_id ON sys_job_log(job_id);
CREATE INDEX IF NOT EXISTS idx_job_log_status ON sys_job_log(status);
CREATE INDEX IF NOT EXISTS idx_job_log_start_time ON sys_job_log(start_time);

-- Comments for documentation
COMMENT ON COLUMN sys_job.max_retry_count IS 'Maximum retry count for failed jobs';
COMMENT ON COLUMN sys_job.retry_interval IS 'Retry interval in seconds';
COMMENT ON COLUMN sys_job.timeout_seconds IS 'Timeout in seconds';
COMMENT ON COLUMN sys_job.notify_on_failure IS 'Enable email notification on failure';
COMMENT ON COLUMN sys_job.notification_emails IS 'Email addresses for notifications (comma-separated)';
COMMENT ON COLUMN sys_job.webhook_url IS 'Webhook URL for notifications';
COMMENT ON COLUMN sys_job.dependent_job_ids IS 'Dependent job IDs (comma-separated)';
COMMENT ON COLUMN sys_job.time_zone IS 'Time zone for scheduling';
COMMENT ON COLUMN sys_job.allow_holiday IS 'Allow execution on holidays';
COMMENT ON COLUMN sys_job.template_name IS 'Job template name';

COMMENT ON COLUMN sys_job_log.job_id IS 'Task ID';
COMMENT ON COLUMN sys_job_log.execution_duration IS 'Execution duration in milliseconds';
COMMENT ON COLUMN sys_job_log.retry_count IS 'Retry count';
COMMENT ON COLUMN sys_job_log.create_time IS 'Create time';
