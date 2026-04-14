-- Add dataTables JSON column to email template for multiple data tables
ALTER TABLE sys_job_email_template ADD COLUMN IF NOT EXISTS data_tables TEXT;
