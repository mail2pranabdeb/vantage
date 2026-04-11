package com.pd.framework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for notification services (webhooks)
 */
@Configuration
public class NotificationConfig {

    // Note: javaMailSender is now configured in MailConfig (vantage-admin)
    // which loads SMTP settings from sys_config table at startup.

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
