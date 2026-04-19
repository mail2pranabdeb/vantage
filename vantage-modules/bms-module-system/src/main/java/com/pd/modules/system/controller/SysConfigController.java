package com.pd.modules.system.controller;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysConfig;
import com.pd.modules.system.infrastructure.repository.SysConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * System configuration controller with batch operations
 */
@RestController
@RequestMapping("/api/system/config")
public class SysConfigController extends BaseController {

    @Autowired
    private SysConfigRepository configRepository;


    /**
     * Get all configurations
     */
    @GetMapping("/list")
    public AjaxResult list() {
        return success(configRepository.findAll());
    }

    /**
     * Batch save configurations
     */
    @PostMapping("/batch")
    public AjaxResult batchSave(@RequestBody List<Map<String, Object>> configs) {
        int savedCount = 0;
        
        for (Map<String, Object> configData : configs) {
            String configKey = (String) configData.get("configKey");
            String configValue = (String) configData.get("configValue");
            
            if (configKey != null && configValue != null) {
                // Check if config exists
                SysConfig existingConfig = configRepository.findByConfigKey(configKey).orElse(null);
                
                if (existingConfig != null) {
                    // Update existing
                    existingConfig.setConfigValue(configValue);
                    existingConfig.setUpdateTime(LocalDateTime.now());
                    existingConfig.setUpdateBy("admin");
                    configRepository.save(existingConfig);
                } else {
                    // Create new
                    SysConfig newConfig = new SysConfig();
                    newConfig.setConfigName(configKey.replace('.', ' ').replace('_', ' '));
                    newConfig.setConfigKey(configKey);
                    newConfig.setConfigValue(configValue);
                    newConfig.setConfigType("Y");
                    newConfig.setCreateBy("admin");
                    newConfig.setCreateTime(LocalDateTime.now());
                    newConfig.setUpdateBy("admin");
                    newConfig.setUpdateTime(LocalDateTime.now());
                    newConfig.setRemark("System setting");
                    configRepository.save(newConfig);
                }
                savedCount++;
            }
        }
        
        return success("Saved " + savedCount + " configuration(s)");
    }

    /**
     * Test email connection
     */
    @PostMapping("/test-email")
    public AjaxResult testEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String host = request.get("host");
        String port = request.get("port");
        String username = request.get("username");
        String password = request.get("password");
        String enableAuth = request.get("enableAuth");
        String enableTls = request.get("enableTls");
        
        if (to == null || to.isEmpty()) {
            return error("Recipient email is required");
        }
        
        if (host == null || host.isEmpty()) {
            return error("SMTP host is required");
        }
        
        try {
            // Create temporary mail sender with provided settings
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(Integer.parseInt(port != null ? port : "587"));
            mailSender.setUsername(username != null ? username : "");
            mailSender.setPassword(password != null ? password : "");
            
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true".equals(enableAuth));
            
            // Fix: Properly enable STARTTLS for port 587
            if ("true".equals(enableTls)) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                // Let Java negotiate TLS version automatically
                props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            } else {
                // For SSL on port 465
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", port != null ? port : "465");
                props.put("mail.smtp.socketFactory.fallback", "false");
            }
            
            // Connection timeout settings
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "30000");
            
            // Enable debug for troubleshooting
            props.put("mail.debug", "false");
            
            System.out.println("=== Testing Email Connection ===");
            System.out.println("Host: " + host);
            System.out.println("Port: " + port);
            System.out.println("TLS: " + enableTls);
            System.out.println("Auth: " + enableAuth);
            
            // Send test email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Vantage Admin <test@vantage.com>");
            message.setTo(to);
            message.setSubject("Test Email from Vantage Admin");
            message.setText("This is a test email to verify your SMTP configuration.\n\nSMTP Settings:\n" +
                          "Host: " + host + "\n" +
                          "Port: " + port + "\n" +
                          "Auth: " + enableAuth + "\n" +
                          "TLS: " + enableTls + "\n\n" +
                          "If you received this, your email configuration is working correctly!\n\n" +
                          "Best regards,\n" +
                          "Vantage Admin");
            
            mailSender.send(message);
            
            return success("Test email sent successfully to " + to + "! Check your inbox.");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("STARTTLS")) {
                return error("STARTTLS error: Please enable TLS/SSL and use port 587. For Gmail, use App Password.");
            } else if (errorMsg.contains("Authentication")) {
                return error("Authentication failed: Use Gmail App Password, not regular password.");
            }
            return error("Failed to send test email: " + errorMsg);
        }
    }

    /**
     * Get config by key
     */
    @GetMapping("/key/{configKey}")
    public AjaxResult getByKey(@PathVariable String configKey) {
        return configRepository.findByConfigKey(configKey)
                .map(this::success)
                .orElse(error("Config not found"));
    }

    /**
     * Update single config
     */
    @PutMapping
    public AjaxResult update(@RequestBody SysConfig config) {
        SysConfig existing = configRepository.findByConfigKey(config.getConfigKey()).orElse(null);
        if (existing == null) {
            return error("Config not found");
        }
        
        existing.setConfigValue(config.getConfigValue());
        existing.setUpdateTime(LocalDateTime.now());
        existing.setUpdateBy("admin");
        configRepository.save(existing);
        
        return success("Configuration updated");
    }

    /**
     * Delete config by ID
     */
    @DeleteMapping("/{configId}")
    public AjaxResult remove(@PathVariable Long configId) {
        if (!configRepository.findById(configId).isPresent()) {
            return error("Config not found");
        }
        configRepository.deleteById(configId);
        return success("Config deleted");
    }
}
