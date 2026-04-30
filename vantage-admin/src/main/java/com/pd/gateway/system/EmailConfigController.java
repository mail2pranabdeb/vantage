package com.pd.gateway.system;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysConfig;
import com.pd.modules.system.infrastructure.repository.SysConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Email configuration controller
 * Reads/writes SMTP settings to sys_config table
 */
@RestController
@RequestMapping("/api/system/email-config")
public class EmailConfigController extends BaseController {

    @Autowired
    private SysConfigRepository configRepository;

    private final JavaMailSender mailSender;

    public EmailConfigController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Get email configuration from database
     */
    @GetMapping
    public AjaxResult getConfig() {
        Map<String, Object> config = new HashMap<>();
        // Check new keys first, fallback to legacy keys (mail.host, etc.)
        config.put("host", getConfigValue("mail.smtp.host", getConfigValue("mail.host", "")));
        config.put("port", getConfigValue("mail.smtp.port", getConfigValue("mail.port", "587")));
        config.put("username", getConfigValue("mail.smtp.username", getConfigValue("mail.username", "")));
        config.put("password", getConfigValue("mail.smtp.password", getConfigValue("mail.password", "")));
        config.put("enableAuth", "true".equals(getConfigValue("mail.smtp.auth", getConfigValue("mail.enableAuth", "true"))));
        config.put("enableTls", "true".equals(getConfigValue("mail.smtp.starttls.enable", getConfigValue("mail.enableTls", "true"))));
        config.put("fromEmail", getConfigValue("mail.smtp.fromEmail", getConfigValue("mail.fromEmail", "")));
        config.put("fromName", getConfigValue("mail.smtp.fromName", getConfigValue("mail.fromName", "")));
        return success(config);
    }

    private String getConfigValue(String key, String defaultVal) {
        return configRepository.findByConfigKey(key)
            .map(SysConfig::getConfigValue)
            .orElse(defaultVal);
    }

    /**
     * Save email configuration to sys_config table
     */
    @PostMapping
    public AjaxResult saveConfig(@RequestBody Map<String, Object> config) {
        try {
            saveOrUpdateConfig("mail.smtp.host", (String) config.get("host"), "SMTP Host");
            saveOrUpdateConfig("mail.smtp.port", (String) config.get("port"), "SMTP Port");
            saveOrUpdateConfig("mail.smtp.username", (String) config.get("username"), "SMTP Username");
            saveOrUpdateConfig("mail.smtp.password", (String) config.get("password"), "SMTP Password");
            saveOrUpdateConfig("mail.smtp.auth", "true".equals(config.get("enableAuth")) ? "true" : "false", "Enable Authentication");
            saveOrUpdateConfig("mail.smtp.starttls.enable", "true".equals(config.get("enableTls")) ? "true" : "false", "Enable STARTTLS");
            if (config.get("fromEmail") != null) {
                saveOrUpdateConfig("mail.smtp.fromEmail", (String) config.get("fromEmail"), "SMTP From Email");
            }
            if (config.get("fromName") != null) {
                saveOrUpdateConfig("mail.smtp.fromName", (String) config.get("fromName"), "SMTP From Name");
            }
            return success("Email configuration saved successfully");
        } catch (Exception e) {
            return error("Failed to save config: " + e.getMessage());
        }
    }

    private void saveOrUpdateConfig(String key, String value, String name) {
        SysConfig existing = configRepository.findByConfigKey(key).orElse(null);
        if (existing != null) {
            existing.setConfigValue(value);
            existing.setUpdateTime(LocalDateTime.now());
            existing.setUpdateBy("admin");
            configRepository.save(existing);
        } else {
            SysConfig newConfig = new SysConfig();
            newConfig.setConfigName(name);
            newConfig.setConfigKey(key);
            newConfig.setConfigValue(value);
            newConfig.setConfigType("Y");
            newConfig.setCreateBy("admin");
            newConfig.setCreateTime(LocalDateTime.now());
            newConfig.setUpdateBy("admin");
            newConfig.setUpdateTime(LocalDateTime.now());
            newConfig.setRemark("Email SMTP setting");
            configRepository.save(newConfig);
        }
    }

    /**
     * Send test email using the globally configured JavaMailSender
     */
    @PostMapping("/test")
    public AjaxResult sendTest(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        if (to == null || to.isEmpty()) {
            return error("Recipient email is required");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String fromEmail = "noreply@vantage.com";
            if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl) {
                String user = ((org.springframework.mail.javamail.JavaMailSenderImpl) mailSender).getUsername();
                if (user != null && !user.isEmpty()) {
                    fromEmail = user;
                }
            }
            message.setFrom("Vantage Admin <" + fromEmail + ">");
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
