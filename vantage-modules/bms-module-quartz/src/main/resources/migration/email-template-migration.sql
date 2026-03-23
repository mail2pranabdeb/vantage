-- Email Template Table for Job Notifications
CREATE TABLE IF NOT EXISTS sys_job_email_template (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(64) NOT NULL UNIQUE,
    template_type VARCHAR(32) NOT NULL,
    email_subject VARCHAR(255) NOT NULL,
    email_body TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark VARCHAR(500),
    INDEX idx_template_type (template_type),
    INDEX idx_is_active (is_active),
    INDEX idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Email templates for job notifications';

-- Insert default templates
INSERT INTO sys_job_email_template (template_name, template_type, email_subject, email_body, is_default, is_active, remark) VALUES
('Job Failure Notification', 'JOB_FAILURE', '[${appName}] Job Failed: ${jobName}', 
'<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;line-height:1.6;color:#333}.container{max-width:600px;margin:0 auto;padding:20px}.header{background:linear-gradient(135deg,#f5576c 0%,#f093fb 100%);color:white;padding:20px;border-radius:8px 8px 0 0}.content{background:#f9f9f9;padding:20px;border:1px solid #e0e0e0}.info-table{width:100%;border-collapse:collapse;margin:15px 0}.info-table td{padding:8px;border-bottom:1px solid #e0e0e0}.info-table td:first-child{font-weight:600;width:140px}.error-box{background:#ffe6e6;border-left:4px solid #f5576c;padding:12px;margin:15px 0;border-radius:4px}.footer{text-align:center;padding:15px;color:#666;font-size:12px}</style></head><body><div class="container"><div class="header"><h2 style="margin:0">❌ Job Execution Failed</h2></div><div class="content"><p>A scheduled job has failed during execution.</p><table class="info-table"><tr><td>Application</td><td>${appName}</td></tr><tr><td>Job ID</td><td>${jobId}</td></tr><tr><td>Job Name</td><td>${jobName}</td></tr><tr><td>Job Group</td><td>${jobGroup}</td></tr><tr><td>Invoke Target</td><td><code>${invokeTarget}</code></td></tr><tr><td>Execution Time</td><td>${executionTime}</td></tr><tr><td>Duration</td><td>${duration} ms</td></tr><tr><td>Retry Count</td><td>${retryCount}</td></tr></table><div class="error-box"><strong>Error Message:</strong><br>${message}</div><div class="error-box"><strong>Exception Details:</strong><br><pre style="white-space:pre-wrap;word-wrap:break-word;font-size:11px">${exceptionInfo}</pre></div></div><div class="footer"><p>This is an automated notification from ${appName}<br>Generated at: ${timestamp}</p></div></div></body></html>', 
TRUE, TRUE, 'Default template for job failure notifications'),

('Job Success Notification', 'JOB_SUCCESS', '[${appName}] Job Completed: ${jobName}', 
'<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;line-height:1.6;color:#333}.container{max-width:600px;margin:0 auto;padding:20px}.header{background:linear-gradient(135deg,#11998e 0%,#38ef7d 100%);color:white;padding:20px;border-radius:8px 8px 0 0}.content{background:#f9f9f9;padding:20px;border:1px solid #e0e0e0}.info-table{width:100%;border-collapse:collapse;margin:15px 0}.info-table td{padding:8px;border-bottom:1px solid #e0e0e0}.info-table td:first-child{font-weight:600;width:140px}.footer{text-align:center;padding:15px;color:#666;font-size:12px}</style></head><body><div class="container"><div class="header"><h2 style="margin:0">✅ Job Completed Successfully</h2></div><div class="content"><p>A scheduled job has completed successfully.</p><table class="info-table"><tr><td>Application</td><td>${appName}</td></tr><tr><td>Job ID</td><td>${jobId}</td></tr><tr><td>Job Name</td><td>${jobName}</td></tr><tr><td>Job Group</td><td>${jobGroup}</td></tr><tr><td>Execution Time</td><td>${executionTime}</td></tr><tr><td>Duration</td><td>${duration} ms</td></tr></table><p><strong>Message:</strong><br>${message}</p></div><div class="footer"><p>This is an automated notification from ${appName}<br>Generated at: ${timestamp}</p></div></div></body></html>', 
TRUE, TRUE, 'Default template for job success notifications'),

('Job Recovery Notification', 'JOB_RECOVERY', '[${appName}] Job Recovered: ${jobName}', 
'<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;line-height:1.6;color:#333}.container{max-width:600px;margin:0 auto;padding:20px}.header{background:linear-gradient(135deg,#4facfe 0%,#00f2fe 100%);color:white;padding:20px;border-radius:8px 8px 0 0}.content{background:#f9f9f9;padding:20px;border:1px solid #e0e0e0}.info-table{width:100%;border-collapse:collapse;margin:15px 0}.info-table td{padding:8px;border-bottom:1px solid #e0e0e0}.info-table td:first-child{font-weight:600;width:140px}.footer{text-align:center;padding:15px;color:#666;font-size:12px}</style></head><body><div class="container"><div class="header"><h2 style="margin:0">🔄 Job Recovered</h2></div><div class="content"><p>A previously failed job has recovered and executed successfully!</p><table class="info-table"><tr><td>Application</td><td>${appName}</td></tr><tr><td>Job ID</td><td>${jobId}</td></tr><tr><td>Job Name</td><td>${jobName}</td></tr><tr><td>Execution Time</td><td>${executionTime}</td></tr><tr><td>Duration</td><td>${duration} ms</td></tr></table></div><div class="footer"><p>This is an automated notification from ${appName}<br>Generated at: ${timestamp}</p></div></div></body></html>', 
TRUE, TRUE, 'Default template for job recovery notifications');
