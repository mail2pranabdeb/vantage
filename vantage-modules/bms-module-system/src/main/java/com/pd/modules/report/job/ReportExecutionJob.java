package com.pd.modules.report.job;

import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.report.domain.SysReportTemplate;
import com.pd.modules.report.service.ReportDesignerService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Quartz job for executing report templates and sending email with attachments.
 * Supports both legacy reports and new designer templates.
 */
@Component
public class ReportExecutionJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(ReportExecutionJob.class);

    private final ReportDesignerService reportDesignerService;
    private final JavaMailSender mailSender;

    public ReportExecutionJob(ReportDesignerService reportDesignerService,
                              JavaMailSender mailSender) {
        this.reportDesignerService = reportDesignerService;
        this.mailSender = mailSender;
    }

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long templateId = dataMap.getLong("templateId");
        String params = dataMap.getString("params");
        String recipients = dataMap.getString("recipients");
        String ccEmails = dataMap.getString("ccEmails");
        String subject = dataMap.getString("subject");
        String body = dataMap.getString("body");
        String format = dataMap.getString("format");

        if (format == null || format.isEmpty()) {
            format = "EXCEL";
        }

        log.info("=== Report Execution Job Started ===");
        log.info("Template ID: {}", templateId);
        log.info("Recipients: {}", recipients);
        log.info("Format: {}", format);

        try {
            // Execute the report
            List<Map<String, Object>> data = reportDesignerService.executeTemplate(templateId, params);
            log.info("Report executed successfully. Rows: {}", data.size());

            // Generate attachment
            byte[] attachmentBytes = reportDesignerService.generateReportAttachment(templateId, params, format);
            String fileName = generateFileName(templateId, format);
            String contentType = getContentType(format);

            // Send email
            if (StringUtils.hasText(recipients)) {
                sendReportEmail(recipients, ccEmails, subject, body, attachmentBytes, fileName, contentType);
                log.info("=== Report email sent successfully ===");
            } else {
                log.warn("=== No recipients configured, skipping email ===");
            }

        } catch (Exception e) {
            log.error("=== Report Execution Job Failed ===", e);
            throw new RuntimeException("Report execution job failed", e);
        }
    }

    /**
     * Execute template and send email with report attachment.
     * Used by the legacy SysReportService for backwards compatibility.
     */
    public void executeAndEmailTemplate(Long templateId, String format, String[] recipients, String[] ccEmails,
                                        String subject, String body, String params) {
        try {
            // Execute the report
            List<Map<String, Object>> data = reportDesignerService.executeTemplate(templateId, params);
            log.info("Template {} executed. Rows: {}", templateId, data.size());

            // Generate attachment
            byte[] attachmentBytes = reportDesignerService.generateReportAttachment(templateId, params, format);
            String fileName = generateFileName(templateId, format);
            String contentType = getContentType(format);

            // Send email
            if (recipients != null && recipients.length > 0) {
                String recipientStr = String.join(",", recipients);
                String ccStr = ccEmails != null ? String.join(",", ccEmails) : null;
                sendReportEmail(recipientStr, ccStr, subject, body, attachmentBytes, fileName, contentType);
                log.info("=== Report email sent successfully ===");
            }
        } catch (Exception e) {
            log.error("Failed to execute and email template {}", templateId, e);
            throw new RuntimeException("Failed to execute and email template", e);
        }
    }

    private void sendReportEmail(String recipients, String ccEmails, String subject, String body,
                                 byte[] attachment, String fileName, String contentType) throws MessagingException {
        if (subject == null || subject.isEmpty()) {
            subject = "Report: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        if (body == null || body.isEmpty()) {
            body = "Please find the attached report.";
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(recipients.split(","));
        if (ccEmails != null && !ccEmails.isEmpty()) {
            helper.setCc(ccEmails.split(","));
        }
        helper.setSubject(subject);
        helper.setText(body, true); // HTML body
        helper.setFrom("noreply@vantage.com");

        // Add attachment
        ByteArrayResource resource = new ByteArrayResource(attachment) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        helper.addAttachment(fileName, resource, contentType);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Failed to send report email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String generateFileName(Long templateId, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Map<String, Object> info = reportDesignerService.getTemplateInfoForEmail(templateId);
        String templateName = (String) info.getOrDefault("templateName", "Report_" + templateId);
        String extension = getFileExtension(format);
        return templateName + "_" + timestamp + "." + extension;
    }

    private String getFileExtension(String format) {
        switch (format.toUpperCase()) {
            case "CSV": return "csv";
            case "JSON": return "json";
            case "HTML": return "html";
            case "EXCEL": default: return "xls";
        }
    }

    private String getContentType(String format) {
        switch (format.toUpperCase()) {
            case "CSV": return "text/csv";
            case "JSON": return "application/json";
            case "HTML": return "text/html";
            case "EXCEL": default: return "application/vnd.ms-excel";
        }
    }
}
