package com.pd.modules.system.api;

import org.springframework.mail.javamail.JavaMailSender;

/**
 * System module public API for mail operations.
 */
public interface SystemMailService {

    JavaMailSender getMailSender();

    String getConfigValue(String... keys);

    void clearCache();
}
