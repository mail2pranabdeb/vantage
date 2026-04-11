package com.pd.config;

import com.pd.modules.system.domain.SysConfig;
import com.pd.modules.system.infrastructure.repository.SysConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Dynamic Mail Configuration.
 * Loads SMTP settings from sys_config table at startup.
 * Supports both new keys (mail.smtp.*) and legacy keys (mail.*) for backward compatibility.
 */
@Configuration
public class MailConfig {

    @Autowired(required = false)
    private SysConfigRepository configRepository;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MailConfig.class);
        
        if (configRepository != null) {
            try {
                // Check for new keys first, then fallback to legacy keys found in user's DB
                // Legacy keys: mail.host, mail.port, etc.
                String host = getConfig("mail.smtp.host", "mail.host");
                
                if (host != null && !host.isEmpty()) {
                    log.info("=== Found SMTP Host: {} ===", host);
                    String port = getConfig("mail.smtp.port", "mail.port");
                    String username = getConfig("mail.smtp.username", "mail.username");
                    String password = getConfig("mail.smtp.password", "mail.password");
                    String auth = getConfig("mail.smtp.auth", "mail.enableAuth");
                    String tls = getConfig("mail.smtp.starttls.enable", "mail.enableTls");

                    sender.setHost(host);
                    sender.setPort(port != null ? Integer.parseInt(port) : 587);
                    sender.setUsername(username != null ? username : "");
                    // Password logging commented out for security, but verify it's not null
                    log.info("=== Mail Sender Configured Successfully ===");
                    log.info("   Host: {}, Port: {}, User: {}", host, port, username);
                    
                    Properties props = sender.getJavaMailProperties();
                    props.put("mail.transport.protocol", "smtp");
                    props.put("mail.smtp.auth", auth != null ? auth : "true");
                    props.put("mail.smtp.starttls.enable", tls != null ? tls : "true");
                    props.put("mail.smtp.starttls.required", tls != null ? tls : "true");
                    props.put("mail.smtp.connectiontimeout", "10000");
                    props.put("mail.smtp.timeout", "30000");
                    
                    return sender;
                } else {
                    log.warn("=== SMTP Host not found in DB. Using localhost:25 fallback ===");
                }
            } catch (Exception e) {
                log.error("=== Error configuring Mail Sender ===", e);
            }
        } else {
            log.warn("=== ConfigRepository is null ===");
        }
        
        // Default fallback
        sender.setHost("localhost");
        sender.setPort(25);
        return sender;
    }

    private String getConfig(String key1, String key2) {
        return configRepository.findByConfigKey(key1)
            .map(SysConfig::getConfigValue)
            .orElseGet(() -> configRepository.findByConfigKey(key2)
                .map(SysConfig::getConfigValue)
                .orElse(null));
    }
}
