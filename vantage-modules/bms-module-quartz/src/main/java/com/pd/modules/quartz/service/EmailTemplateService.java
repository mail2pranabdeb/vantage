package com.pd.modules.quartz.service;

import com.pd.modules.quartz.domain.EmailTemplate;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.domain.SysJobLog;
import com.pd.modules.quartz.infrastructure.repository.EmailTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing email templates and sending templated notifications
 */
@Service
public class EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateRepository templateRepository;

    @Autowired(required = false)
    private org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate namedJdbcTemplate;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.name:Vantage}")
    private String appName;

    public EmailTemplateService(JavaMailSender mailSender, EmailTemplateRepository templateRepository) {
        this.mailSender = mailSender;
        this.templateRepository = templateRepository;
    }

    /**
     * Get all templates
     */
    public List<EmailTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    /**
     * Get active templates
     */
    public List<EmailTemplate> getActiveTemplates() {
        return templateRepository.findActive();
    }

    /**
     * Get template by ID
     */
    public Optional<EmailTemplate> getTemplateById(Long templateId) {
        return templateRepository.findById(templateId);
    }

    /**
     * Get template by type
     */
    public Optional<EmailTemplate> getTemplateByType(String templateType) {
        return templateRepository.findDefaultByType(templateType)
                .or(() -> templateRepository.findByType(templateType));
    }

    /**
     * Save template
     */
    public EmailTemplate saveTemplate(EmailTemplate template) {
        return templateRepository.save(template);
    }

    /**
     * Delete template
     */
    public void deleteTemplate(Long templateId) {
        templateRepository.deleteById(templateId);
    }

    /**
     * Set template as default
     */
    public void setTemplateAsDefault(Long templateId, String templateType) {
        templateRepository.setAsDefault(templateId, templateType);
    }

    /**
     * Send email using template
     */
    public void sendTemplatedEmail(String templateType, String[] recipients, SysJob job, SysJobLog jobLog) {
        Optional<EmailTemplate> templateOpt;
        
        // Use job's selected template if available, otherwise find by type
        if (job.getEmailTemplateId() != null) {
            templateOpt = getTemplateById(job.getEmailTemplateId());
        } else {
            templateOpt = getTemplateByType(templateType);
        }
        
        if (templateOpt.isEmpty()) {
            log.warn("No email template found for type: {} or job template ID: {}", templateType, job.getEmailTemplateId());
            return;
        }

        EmailTemplate template = templateOpt.get();
        
        try {
            // Process template with variables
            String subject = processTemplate(template.getEmailSubject(), job, jobLog);
            String body = processTemplate(template.getEmailBody(), job, jobLog);

            // Send HTML email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(recipients);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            mailSender.send(message);
            log.info("Templated email sent for job {} using template {}", job.getJobId(), template.getTemplateName());
        } catch (Exception e) {
            log.error("Failed to send templated email for job {}", job.getJobId(), e);
        }
    }

    /**
     * Process template variables (public version for preview, uses sample data)
     */
    public String processTemplate(String template, SysJob job, SysJobLog jobLog) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        if (job == null) {
            // Use sample data for preview
            return template
                .replace("${appName}", appName)
                .replace("${jobId}", "1")
                .replace("${jobName}", "Sample Report Job")
                .replace("${jobGroup}", "system")
                .replace("${invokeTarget}", "reportService.execute()")
                .replace("${cronExpression}", "0 */5 * * * ?")
                .replace("${executionTime}", now.format(formatter))
                .replace("${duration}", "1234")
                .replace("${retryCount}", "0")
                .replace("${status}", "Success")
                .replace("${message}", "Execution completed successfully")
                .replace("${exceptionInfo}", "N/A")
                .replace("${timestamp}", now.format(formatter))
                .replace("${reportName}", "Sample Report")
                .replace("${reportFormat}", "CSV")
                .replace("${totalRows}", "10");
        }

        return template
            .replace("${appName}", appName)
            .replace("${jobId}", String.valueOf(job.getJobId()))
            .replace("${jobName}", job.getJobName())
            .replace("${jobGroup}", job.getJobGroup())
            .replace("${invokeTarget}", job.getInvokeTarget())
            .replace("${cronExpression}", job.getCronExpression() != null ? job.getCronExpression() : "N/A")
            .replace("${executionTime}", jobLog != null && jobLog.getStartTime() != null ? jobLog.getStartTime().format(formatter) : "N/A")
            .replace("${duration}", String.valueOf(jobLog != null && jobLog.getExecutionDuration() != null ? jobLog.getExecutionDuration() : 0))
            .replace("${retryCount}", String.valueOf(jobLog != null && jobLog.getRetryCount() != null ? jobLog.getRetryCount() : 0))
            .replace("${status}", jobLog != null && jobLog.getStatus() != null ? ("0".equals(jobLog.getStatus()) ? "Success" : "Failed") : "Unknown")
            .replace("${message}", jobLog != null && jobLog.getJobMessage() != null ? jobLog.getJobMessage() : "N/A")
            .replace("${exceptionInfo}", jobLog != null && jobLog.getExceptionInfo() != null ? jobLog.getExceptionInfo() : "N/A")
            .replace("${timestamp}", now.format(formatter))
            .replace("${reportName}", job.getJobName())
            .replace("${reportFormat}", "CSV")
            .replace("${totalRows}", "0");
    }

    /**
     * Execute SQL query against a datasource and render results as HTML table
     */
    public String executeQueryAndRenderTable(String datasourceKey, String querySql) {
        if (namedJdbcTemplate == null) {
            log.warn("NamedParameterJdbcTemplate not available, cannot execute query for datasource: {}", datasourceKey);
            return "<p style='color:red;'>Error: Datasource query execution not available</p>";
        }

        try {
            // For now, use the primary datasource (H2). In a full multi-datasource setup,
            // you'd resolve the datasource by key and create a dynamic JdbcTemplate.
            List<Map<String, Object>> rows = namedJdbcTemplate.queryForList(querySql, java.util.Collections.emptyMap());

            if (rows.isEmpty()) {
                return "<p>No data returned from query.</p>";
            }

            StringBuilder html = new StringBuilder();
            html.append("<table style='width:100%;border-collapse:collapse;font-size:12px;font-family:Arial,sans-serif;'>");
            html.append("<thead><tr style='background:#4a5568;color:white;'>");

            // Headers
            Map<String, Object> firstRow = rows.get(0);
            for (String col : firstRow.keySet()) {
                html.append("<th style='padding:8px;border:1px solid #e2e8f0;text-align:left;'>")
                    .append(escapeHtml(col))
                    .append("</th>");
            }
            html.append("</tr></thead><tbody>");

            // Data rows
            int rowCount = 0;
            for (Map<String, Object> row : rows) {
                if (rowCount >= 500) { // Limit to 500 rows for email
                    break;
                }
                html.append("<tr style='background:")
                    .append(rowCount % 2 == 0 ? "#f7fafc" : "white")
                    .append(";'>");
                for (String col : firstRow.keySet()) {
                    Object val = row.get(col);
                    html.append("<td style='padding:6px 8px;border:1px solid #e2e8f0;'>")
                        .append(escapeHtml(val != null ? val.toString() : ""))
                        .append("</td>");
                }
                html.append("</tr>");
                rowCount++;
            }

            html.append("</tbody></table>");
            if (rows.size() > 500) {
                html.append("<p style='font-size:11px;color:#666;'>Showing first 500 of ")
                    .append(rows.size()).append(" rows.</p>");
            }

            log.info("Executed query for datasource '{}', rendered {} rows as HTML table", datasourceKey, rowCount);
            return html.toString();
        } catch (Exception e) {
            log.error("Failed to execute query for datasource: {}", datasourceKey, e);
            return "<p style='color:red;'>Error executing query: " + escapeHtml(e.getMessage()) + "</p>";
        }
    }

    /**
     * Escape HTML special characters
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * Process template variables (private version with real job data)
     */
    private String processTemplateInternal(String template, SysJob job, SysJobLog jobLog) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return template
            .replace("${appName}", appName)
            .replace("${jobId}", String.valueOf(job.getJobId()))
            .replace("${jobName}", job.getJobName())
            .replace("${jobGroup}", job.getJobGroup())
            .replace("${invokeTarget}", job.getInvokeTarget())
            .replace("${cronExpression}", job.getCronExpression() != null ? job.getCronExpression() : "N/A")
            .replace("${executionTime}", jobLog.getStartTime() != null ? jobLog.getStartTime().format(formatter) : "N/A")
            .replace("${duration}", String.valueOf(jobLog.getExecutionDuration() != null ? jobLog.getExecutionDuration() : 0))
            .replace("${retryCount}", String.valueOf(jobLog.getRetryCount() != null ? jobLog.getRetryCount() : 0))
            .replace("${status}", jobLog.getStatus() != null ? ("0".equals(jobLog.getStatus()) ? "Success" : "Failed") : "Unknown")
            .replace("${message}", jobLog.getJobMessage() != null ? jobLog.getJobMessage() : "N/A")
            .replace("${exceptionInfo}", jobLog.getExceptionInfo() != null ? jobLog.getExceptionInfo() : "N/A")
            .replace("${timestamp}", LocalDateTime.now().format(formatter));
    }

    /**
     * Initialize default templates
     */
    public void initializeDefaultTemplates() {
        // Check if templates already exist
        if (!templateRepository.findAll().isEmpty()) {
            return;
        }

        // Job Failure Template
        EmailTemplate failureTemplate = new EmailTemplate();
        failureTemplate.setTemplateName("Job Failure Notification");
        failureTemplate.setTemplateType("JOB_FAILURE");
        failureTemplate.setEmailSubject("[${appName}] Job Failed: ${jobName}");
        failureTemplate.setEmailBody(getDefaultFailureTemplate());
        failureTemplate.setIsDefault(true);
        failureTemplate.setIsActive(true);
        failureTemplate.setRemark("Default template for job failure notifications");
        templateRepository.save(failureTemplate);

        // Job Success Template
        EmailTemplate successTemplate = new EmailTemplate();
        successTemplate.setTemplateName("Job Success Notification");
        successTemplate.setTemplateType("JOB_SUCCESS");
        successTemplate.setEmailSubject("[${appName}] Job Completed: ${jobName}");
        successTemplate.setEmailBody(getDefaultSuccessTemplate());
        successTemplate.setIsDefault(true);
        successTemplate.setIsActive(true);
        successTemplate.setRemark("Default template for job success notifications");
        templateRepository.save(successTemplate);

        // Job Recovery Template
        EmailTemplate recoveryTemplate = new EmailTemplate();
        recoveryTemplate.setTemplateName("Job Recovery Notification");
        recoveryTemplate.setTemplateType("JOB_RECOVERY");
        recoveryTemplate.setEmailSubject("[${appName}] Job Recovered: ${jobName}");
        recoveryTemplate.setEmailBody(getDefaultRecoveryTemplate());
        recoveryTemplate.setIsDefault(true);
        recoveryTemplate.setIsActive(true);
        recoveryTemplate.setRemark("Default template for job recovery after failure");
        templateRepository.save(recoveryTemplate);

        log.info("Initialized default email templates");
    }

    /**
     * Get default failure email HTML template
     */
    private String getDefaultFailureTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%); color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #e0e0e0; }
                    .info-table { width: 100%; border-collapse: collapse; margin: 15px 0; }
                    .info-table td { padding: 8px; border-bottom: 1px solid #e0e0e0; }
                    .info-table td:first-child { font-weight: 600; width: 140px; }
                    .error-box { background: #ffe6e6; border-left: 4px solid #f5576c; padding: 12px; margin: 15px 0; border-radius: 4px; }
                    .footer { text-align: center; padding: 15px; color: #666; font-size: 12px; }
                    .badge { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
                    .badge-failed { background: #f5576c; color: white; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2 style="margin: 0;">❌ Job Execution Failed</h2>
                    </div>
                    <div class="content">
                        <p>A scheduled job has failed during execution. Please review the details below:</p>
                        
                        <table class="info-table">
                            <tr><td>Application</td><td>${appName}</td></tr>
                            <tr><td>Job ID</td><td>${jobId}</td></tr>
                            <tr><td>Job Name</td><td>${jobName}</td></tr>
                            <tr><td>Job Group</td><td>${jobGroup}</td></tr>
                            <tr><td>Invoke Target</td><td><code>${invokeTarget}</code></td></tr>
                            <tr><td>Cron Expression</td><td><code>${cronExpression}</code></td></tr>
                            <tr><td>Execution Time</td><td>${executionTime}</td></tr>
                            <tr><td>Duration</td><td>${duration} ms</td></tr>
                            <tr><td>Retry Count</td><td>${retryCount}</td></tr>
                            <tr><td>Status</td><td><span class="badge badge-failed">FAILED</span></td></tr>
                        </table>
                        
                        <div class="error-box">
                            <strong>Error Message:</strong><br>
                            ${message}
                        </div>
                        
                        <div class="error-box">
                            <strong>Exception Details:</strong><br>
                            <pre style="white-space: pre-wrap; word-wrap: break-word; font-size: 11px;">${exceptionInfo}</pre>
                        </div>
                        
                        <p style="color: #666; font-size: 13px;">Please investigate and take appropriate action.</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated notification from ${appName}<br>
                        Generated at: ${timestamp}</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    /**
     * Get default success email HTML template
     */
    private String getDefaultSuccessTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #e0e0e0; }
                    .info-table { width: 100%; border-collapse: collapse; margin: 15px 0; }
                    .info-table td { padding: 8px; border-bottom: 1px solid #e0e0e0; }
                    .info-table td:first-child { font-weight: 600; width: 140px; }
                    .footer { text-align: center; padding: 15px; color: #666; font-size: 12px; }
                    .badge { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
                    .badge-success { background: #11998e; color: white; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2 style="margin: 0;">✅ Job Completed Successfully</h2>
                    </div>
                    <div class="content">
                        <p>A scheduled job has completed successfully. Details below:</p>
                        
                        <table class="info-table">
                            <tr><td>Application</td><td>${appName}</td></tr>
                            <tr><td>Job ID</td><td>${jobId}</td></tr>
                            <tr><td>Job Name</td><td>${jobName}</td></tr>
                            <tr><td>Job Group</td><td>${jobGroup}</td></tr>
                            <tr><td>Invoke Target</td><td><code>${invokeTarget}</code></td></tr>
                            <tr><td>Execution Time</td><td>${executionTime}</td></tr>
                            <tr><td>Duration</td><td>${duration} ms</td></tr>
                            <tr><td>Status</td><td><span class="badge badge-success">SUCCESS</span></td></tr>
                        </table>
                        
                        <p style="color: #666; font-size: 13px; margin-top: 20px;">
                            <strong>Message:</strong><br>
                            ${message}
                        </p>
                    </div>
                    <div class="footer">
                        <p>This is an automated notification from ${appName}<br>
                        Generated at: ${timestamp}</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    /**
     * Get default recovery email HTML template
     */
    private String getDefaultRecoveryTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #e0e0e0; }
                    .info-table { width: 100%; border-collapse: collapse; margin: 15px 0; }
                    .info-table td { padding: 8px; border-bottom: 1px solid #e0e0e0; }
                    .info-table td:first-child { font-weight: 600; width: 140px; }
                    .footer { text-align: center; padding: 15px; color: #666; font-size: 12px; }
                    .badge { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
                    .badge-recovered { background: #4facfe; color: white; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2 style="margin: 0;">🔄 Job Recovered</h2>
                    </div>
                    <div class="content">
                        <p>A previously failed job has recovered and executed successfully!</p>
                        
                        <table class="info-table">
                            <tr><td>Application</td><td>${appName}</td></tr>
                            <tr><td>Job ID</td><td>${jobId}</td></tr>
                            <tr><td>Job Name</td><td>${jobName}</td></tr>
                            <tr><td>Execution Time</td><td>${executionTime}</td></tr>
                            <tr><td>Duration</td><td>${duration} ms</td></tr>
                            <tr><td>Status</td><td><span class="badge badge-recovered">RECOVERED</span></td></tr>
                        </table>
                        
                        <p style="color: #666; font-size: 13px; margin-top: 20px;">
                            The job is now functioning normally.
                        </p>
                    </div>
                    <div class="footer">
                        <p>This is an automated notification from ${appName}<br>
                        Generated at: ${timestamp}</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}
