package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemConfigService;
import com.pd.modules.system.api.SystemEmailConfigService;
import com.pd.modules.system.api.dto.ConfigDTO;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SystemEmailConfigServiceImpl implements SystemEmailConfigService {

    private final SystemConfigService systemConfigService;
    private final JavaMailSender mailSender;

    public SystemEmailConfigServiceImpl(SystemConfigService systemConfigService, JavaMailSender mailSender) {
        this.systemConfigService = systemConfigService;
        this.mailSender = mailSender;
    }

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", getConfigValue("mail.smtp.host", getConfigValue("mail.host", "")));
        config.put("port", getConfigValue("mail.smtp.port", getConfigValue("mail.port", "587")));
        config.put("username", getConfigValue("mail.smtp.username", getConfigValue("mail.username", "")));
        config.put("password", getConfigValue("mail.smtp.password", getConfigValue("mail.password", "")));
        config.put("enableAuth", "true".equals(getConfigValue("mail.smtp.auth", getConfigValue("mail.enableAuth", "true"))));
        config.put("enableTls", "true".equals(getConfigValue("mail.smtp.starttls.enable", getConfigValue("mail.enableTls", "true"))));
        config.put("fromEmail", getConfigValue("mail.smtp.fromEmail", getConfigValue("mail.fromEmail", "")));
        config.put("fromName", getConfigValue("mail.smtp.fromName", getConfigValue("mail.fromName", "")));
        return config;
    }

    private String getConfigValue(String key, String defaultVal) {
        return systemConfigService.findByConfigKey(key)
                .map(ConfigDTO::getConfigValue)
                .orElse(defaultVal);
    }

    @Override
    public String saveConfig(Map<String, Object> config) {
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
            return "Email configuration saved successfully";
        } catch (Exception e) {
            return "Failed to save config: " + e.getMessage();
        }
    }

    private void saveOrUpdateConfig(String key, String value, String name) {
        var existing = systemConfigService.findByConfigKey(key).orElse(null);
        if (existing != null) {
            existing.setConfigValue(value);
            existing.setUpdateBy("admin");
            systemConfigService.updateConfig(existing);
        } else {
            ConfigDTO newConfig = new ConfigDTO();
            newConfig.setConfigName(name);
            newConfig.setConfigKey(key);
            newConfig.setConfigValue(value);
            newConfig.setConfigType("Y");
            newConfig.setCreateBy("admin");
            newConfig.setUpdateBy("admin");
            newConfig.setRemark("Email SMTP setting");
            systemConfigService.createConfig(newConfig);
        }
    }

    @Override
    public String sendTestEmail(String to) {
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
            return "Test email sent successfully!";
        } catch (Exception e) {
            return "Failed to send test email: " + e.getMessage();
        }
    }
}
