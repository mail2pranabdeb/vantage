package com.pd.modules.system.service;

import com.pd.modules.system.api.SystemMailService;
import com.pd.modules.system.infrastructure.repository.SysConfigRepository;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DynamicMailSenderService implements SystemMailService {

    private static final Logger log = LoggerFactory.getLogger(DynamicMailSenderService.class);

    @Autowired
    private SysConfigRepository configRepository;

    private volatile JavaMailSender cachedMailSender;
    private volatile long lastFetchTime = 0;
    private static final long CACHE_DURATION_MS = 60000; // 1 minute cache

    public String getConfigValue(String... keys) {
        for (String key : keys) {
            try {
                String val = configRepository.findByConfigKey(key)
                    .map(c -> c.getConfigValue())
                    .orElse(null);
                if (val != null && !val.isEmpty()) {
                    return val;
                }
            } catch (Exception e) {
                // continue to next key
            }
        }
        return null;
    }

    public JavaMailSender getMailSender() {
        long now = System.currentTimeMillis();
        if (cachedMailSender != null && (now - lastFetchTime) < CACHE_DURATION_MS) {
            return cachedMailSender;
        }

        synchronized (this) {
            // Double-check after acquiring lock
            if (cachedMailSender != null && (now - lastFetchTime) < CACHE_DURATION_MS) {
                return cachedMailSender;
            }

            try {
                cachedMailSender = createMailSender();
                lastFetchTime = System.currentTimeMillis();
                log.info("Dynamic mail sender configured successfully");
            } catch (Exception e) {
                log.error("Failed to create dynamic mail sender, using fallback", e);
                if (cachedMailSender == null) {
                    cachedMailSender = new JavaMailSenderImpl();
                }
            }
            return cachedMailSender;
        }
    }

    private JavaMailSender createMailSender() {
        String host = getConfigValue("mail.host", "mail.smtp.host");
        String portStr = getConfigValue("mail.port", "mail.smtp.port");
        String username = getConfigValue("mail.username", "mail.smtp.username");
        String password = getConfigValue("mail.password", "mail.smtp.password");
        String authStr = getConfigValue("mail.enableAuth", "mail.smtp.auth", "mail.auth");
        String tlsStr = getConfigValue("mail.enableTls", "mail.smtp.starttls.enable", "mail.starttls.enable");

        int port = 587;
        try {
            if (portStr != null && !portStr.isEmpty()) {
                port = Integer.parseInt(portStr);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid port: {}, using default 587", portStr);
        }

        boolean auth = "true".equalsIgnoreCase(authStr);
        boolean tls = "true".equalsIgnoreCase(tlsStr);

        log.info("Creating dynamic mail sender: host={}, port={}, username={}, auth={}, tls={}", 
            host, port, username, auth, tls);

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        if (host != null && !host.isEmpty()) {
            mailSender.setHost(host);
        } else {
            mailSender.setHost("smtp.gmail.com"); // default
        }
        mailSender.setPort(port);
        
        if (username != null && !username.isEmpty()) {
            mailSender.setUsername(username);
        }
        if (password != null && !password.isEmpty()) {
            mailSender.setPassword(password);
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", mailSender.getHost());
        props.put("mail.smtp.port", mailSender.getPort());
        
        if (auth) {
            props.put("mail.smtp.auth", "true");
        }
        
        if (tls) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        
        props.put("mail.smtp.ssl.enable", "false");
        props.put("mail.debug", "false");

        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            props.put("mail.smtp.auth", "true");
            final String finalUsername = username;
            final String finalPassword = password;
            mailSender.setSession(Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(finalUsername, finalPassword);
                }
            }));
        }

        return mailSender;
    }

    public void clearCache() {
        cachedMailSender = null;
        lastFetchTime = 0;
    }
}
