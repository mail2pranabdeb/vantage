package com.pd.modules.system.api;

import java.util.Map;

/**
 * System module public API for email configuration.
 */
public interface SystemEmailConfigService {

    Map<String, Object> getConfig();

    String saveConfig(Map<String, Object> config);

    String sendTestEmail(String to);
}
