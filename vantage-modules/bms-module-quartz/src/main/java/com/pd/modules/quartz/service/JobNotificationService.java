package com.pd.modules.quartz.service;

import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.domain.SysJobLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending job failure notifications via email and webhooks
 */
@Service
public class JobNotificationService {

    private static final Logger log = LoggerFactory.getLogger(JobNotificationService.class);

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.name:Vantage}")
    private String appName;

    public JobNotificationService(JavaMailSender mailSender, RestTemplate restTemplate) {
        this.mailSender = mailSender;
        this.restTemplate = restTemplate;
    }

    /**
     * Send notification for job failure
     */
    public void sendFailureNotification(SysJob job, SysJobLog jobLog) {
        if (job.getNotifyOnFailure() == null || !job.getNotifyOnFailure()) {
            return;
        }

        // Send email notifications
        if (job.getNotificationEmails() != null && !job.getNotificationEmails().isEmpty()) {
            sendEmailNotification(job, jobLog);
        }

        // Send webhook notification
        if (job.getWebhookUrl() != null && !job.getWebhookUrl().isEmpty()) {
            sendWebhookNotification(job, jobLog);
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(SysJob job, SysJobLog jobLog) {
        try {
            String[] recipients = job.getNotificationEmails().split(",");
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipients);
            message.setSubject(String.format("[%s] Job Failed: %s", appName, job.getJobName()));
            message.setText(buildEmailBody(job, jobLog));
            
            mailSender.send(message);
            log.info("Email notification sent for job {}", job.getJobId());
        } catch (Exception e) {
            log.error("Failed to send email notification for job {}", job.getJobId(), e);
        }
    }

    /**
     * Send webhook notification
     */
    private void sendWebhookNotification(SysJob job, SysJobLog jobLog) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String payload = buildWebhookPayload(job, jobLog);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(job.getWebhookUrl(), entity, String.class);
            log.info("Webhook notification sent for job {}", job.getJobId());
        } catch (Exception e) {
            log.error("Failed to send webhook notification for job {}", job.getJobId(), e);
        }
    }

    /**
     * Build email body
     */
    private String buildEmailBody(SysJob job, SysJobLog jobLog) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return String.format(
            "Job Execution Failure Notification\n" +
            "===================================\n\n" +
            "Application: %s\n" +
            "Job ID: %d\n" +
            "Job Name: %s\n" +
            "Job Group: %s\n" +
            "Invoke Target: %s\n" +
            "Execution Time: %s\n" +
            "Duration: %d ms\n" +
            "Retry Count: %d\n\n" +
            "Error Message:\n%s\n\n" +
            "Exception Details:\n%s\n\n" +
            "Please investigate and take appropriate action.",
            appName,
            job.getJobId(),
            job.getJobName(),
            job.getJobGroup(),
            job.getInvokeTarget(),
            jobLog.getStartTime().format(formatter),
            jobLog.getExecutionDuration(),
            jobLog.getRetryCount(),
            jobLog.getJobMessage(),
            jobLog.getExceptionInfo()
        );
    }

    /**
     * Build webhook payload (JSON)
     */
    private String buildWebhookPayload(SysJob job, SysJobLog jobLog) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return String.format(
            "{" +
            "\"event\":\"job_failure\"," +
            "\"application\":\"%s\"," +
            "\"job\":{" +
            "\"id\":%d," +
            "\"name\":\"%s\"," +
            "\"group\":\"%s\"," +
            "\"invokeTarget\":\"%s\"" +
            "}," +
            "\"execution\":{" +
            "\"startTime\":\"%s\"," +
            "\"duration\":%d," +
            "\"retryCount\":%d," +
            "\"message\":\"%s\"," +
            "\"exception\":\"%s\"" +
            "}" +
            "}",
            appName,
            job.getJobId(),
            job.getJobName(),
            job.getJobGroup(),
            escapeJson(job.getInvokeTarget()),
            jobLog.getStartTime().format(formatter),
            jobLog.getExecutionDuration(),
            jobLog.getRetryCount(),
            escapeJson(jobLog.getJobMessage()),
            escapeJson(jobLog.getExceptionInfo())
        );
    }

    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
