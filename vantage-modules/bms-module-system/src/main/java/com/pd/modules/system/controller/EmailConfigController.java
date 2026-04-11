package com.pd.modules.system.controller;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysConfig;
import com.pd.modules.system.infrastructure.repository.SysConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
     * Get email configuration from database or fallback to application.yml
     */
    @GetMapping
    public AjaxResult getConfig() {
        Map<String, Object> config = new HashMap<>();

        // Try to get from sys_config table first
        String host = getConfigValue("mail.smtp.host");
        String port = getConfigValue("mail.smtp.port");
        String username = getConfigValue("mail.smtp.username");
        String password = getConfigValue("mail.smtp.password");
        String auth = getConfigValue("mail.smtp.auth");
        String tls = getConfigValue("mail.smtp.starttls.enable");

        // Fallback to application.yml values
        if (host == null) host = getFallback("spring.mail.host", "");
        if (port == null) port = getFallback("spring.mail.port", "587");
        if (username == null) username = getFallback("spring.mail.username", "");
        if (password == null) password = getFallback("spring.mail.password", "");
        if (auth == null) auth = getFallback("spring.mail.properties.mail.smtp.auth", "false");
        if (tls == null) tls = getFallback("spring.mail.properties.mail.smtp.starttls.enable", "false");

        config.put("host", host);
        config.put("port", port);
        config.put("username", username);
        config.put("password", password);
        config.put("enableAuth", "true".equals(auth));
        config.put("enableTls", "true".equals(tls));
        return success(config);
    }

    private String getConfigValue(String key) {
        return configRepository.findByConfigKey(key)
            .map(SysConfig::getConfigValue)
            .orElse(null);
    }

    @Value("${spring.mail.host:}")
    private String fallbackHost;
    @Value("${spring.mail.port:587}")
    private String fallbackPort;
    @Value("${spring.mail.username:}")
    private String fallbackUsername;
    @Value("${spring.mail.password:}")
    private String fallbackPassword;
    @Value("${spring.mail.properties.mail.smtp.auth:false}")
    private String fallbackAuth;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private String fallbackTls;

    private String getFallback(String key, String def) {
        switch (key) {
            case "spring.mail.host": return fallbackHost.isEmpty() ? def : fallbackHost;
            case "spring.mail.port": return fallbackPort.isEmpty() ? def : fallbackPort;
            case "spring.mail.username": return fallbackUsername.isEmpty() ? def : fallbackUsername;
            case "spring.mail.password": return fallbackPassword.isEmpty() ? def : fallbackPassword;
            case "spring.mail.properties.mail.smtp.auth": return fallbackAuth.isEmpty() ? def : fallbackAuth;
            case "spring.mail.properties.mail.smtp.starttls.enable": return fallbackTls.isEmpty() ? def : fallbackTls;
            default: return def;
        }
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
            saveOrUpdateConfig("mail.smtp.fromEmail", (String) config.get("fromEmail"), "From Email");
            saveOrUpdateConfig("mail.smtp.fromName", (String) config.get("fromName"), "From Name");
            saveOrUpdateConfig("mail.smtp.auth", "true".equals(config.get("enableAuth")) ? "true" : "false", "Enable Authentication");
            saveOrUpdateConfig("mail.smtp.starttls.enable", "true".equals(config.get("enableTls")) ? "true" : "false", "Enable STARTTLS");
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
     * Send test email using dynamic SMTP settings from sys_config
     */
    @PostMapping("/test")
    public AjaxResult sendTest(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        if (to == null || to.isEmpty()) {
            return error("Recipient email is required");
        }

        try {
            // Build mail sender from sys_config
            JavaMailSenderImpl sender = createMailSenderFromConfig();
            if (sender == null) {
                return error("SMTP not configured. Please configure email settings first.");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender.getUsername());
            message.setTo(to);
            message.setSubject("Test Email from Vantage Admin");
            message.setText("This is a test email to verify your SMTP configuration.\n\nIf you received this, your email configuration is working correctly!\n\nBest regards,\nVantage Admin");

            sender.send(message);
            return success("Test email sent successfully!");
        } catch (Exception e) {
            return error("Failed to send test email: " + e.getMessage());
        }
    }

    /**
     * Create JavaMailSender from sys_config values
     */
    private JavaMailSenderImpl createMailSenderFromConfig() {
        String host = getConfigValue("mail.smtp.host");
        String port = getConfigValue("mail.smtp.port");
        String username = getConfigValue("mail.smtp.username");
        String password = getConfigValue("mail.smtp.password");
        String auth = getConfigValue("mail.smtp.auth");
        String tls = getConfigValue("mail.smtp.starttls.enable");

        if (host == null || host.isEmpty()) {
            return null;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(Integer.parseInt(port != null ? port : "587"));
        sender.setUsername(username != null ? username : "");
        sender.setPassword(password != null ? password : "");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true".equals(auth));

        if ("true".equals(tls)) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");

        return sender;
    }
}
