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
 * If database config is unavailable, falls back to localhost:25.
 */
@Configuration
public class MailConfig {

    @Autowired(required = false)
    private SysConfigRepository configRepository;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        
        if (configRepository != null) {
            try {
                String host = getConfig("mail.smtp.host");
                if (host != null && !host.isEmpty()) {
                    String port = getConfig("mail.smtp.port");
                    String username = getConfig("mail.smtp.username");
                    String password = getConfig("mail.smtp.password");
                    String auth = getConfig("mail.smtp.auth");
                    String tls = getConfig("mail.smtp.starttls.enable");

                    sender.setHost(host);
                    sender.setPort(port != null ? Integer.parseInt(port) : 587);
                    sender.setUsername(username != null ? username : "");
                    sender.setPassword(password != null ? password : "");

                    Properties props = sender.getJavaMailProperties();
                    props.put("mail.transport.protocol", "smtp");
                    props.put("mail.smtp.auth", auth != null ? auth : "true");
                    props.put("mail.smtp.starttls.enable", tls != null ? tls : "true");
                    props.put("mail.smtp.starttls.required", tls != null ? tls : "true");
                    props.put("mail.smtp.connectiontimeout", "10000");
                    props.put("mail.smtp.timeout", "30000");
                    
                    return sender;
                }
            } catch (Exception e) {
                // If DB config fails, fall through to default
            }
        }
        
        // Default fallback
        sender.setHost("localhost");
        sender.setPort(25);
        return sender;
    }

    private String getConfig(String key) {
        return configRepository.findByConfigKey(key)
            .map(SysConfig::getConfigValue)
            .orElse(null);
    }
}
