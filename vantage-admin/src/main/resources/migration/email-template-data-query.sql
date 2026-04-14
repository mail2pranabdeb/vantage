-- Add data query columns to email template for dynamic HTML tables
ALTER TABLE sys_job_email_template ADD COLUMN IF NOT EXISTS datasource_key VARCHAR(50);
ALTER TABLE sys_job_email_template ADD COLUMN IF NOT EXISTS query_sql CLOB;
ALTER TABLE sys_job_email_template ADD COLUMN IF NOT EXISTS include_data_table BOOLEAN DEFAULT FALSE;
