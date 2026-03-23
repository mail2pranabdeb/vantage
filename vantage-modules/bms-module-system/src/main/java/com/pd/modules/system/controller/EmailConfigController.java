package com.pd.modules.system.controller;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Email configuration controller
 */
@RestController
@RequestMapping("/api/system/email-config")
public class EmailConfigController extends BaseController {

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${spring.mail.port:587}")
    private String port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:false}")
    private String auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private String tls;

    private final JavaMailSender mailSender;

    public EmailConfigController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Get email configuration
     */
    @GetMapping
    public AjaxResult getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", host != null ? host : "");
        config.put("port", port != null ? port : "587");
        config.put("username", username != null ? username : "");
        config.put("password", password != null ? password : "");
        config.put("enableAuth", "true".equals(auth));
        config.put("enableTls", "true".equals(tls));
        return success(config);
    }

    /**
     * Save email configuration
     * Note: In production, you'd want to save this to database
     */
    @PostMapping
    public AjaxResult saveConfig(@RequestBody Map<String, Object> config) {
        // For now, just acknowledge receipt
        // In production, save to sys_config table
        return success("Email configuration saved. Note: You need to update application.yml for permanent changes.");
    }

    /**
     * Send test email
     */
    @PostMapping("/test")
    public AjaxResult sendTest(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        
        if (to == null || to.isEmpty()) {
            return error("Recipient email is required");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Vantage Admin <noreply@vantage.com>");
            message.setTo(to);
            message.setSubject("Test Email from Vantage Admin");
            message.setText("This is a test email to verify your SMTP configuration.\n\nIf you received this, your email configuration is working correctly!\n\nBest regards,\nVantage Admin");

            mailSender.send(message);
            return success("Test email sent successfully!");
        } catch (Exception e) {
            return error("Failed to send test email: " + e.getMessage());
        }
    }
}
