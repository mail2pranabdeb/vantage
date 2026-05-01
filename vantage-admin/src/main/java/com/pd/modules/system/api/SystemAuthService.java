package com.pd.modules.system.api;

import java.util.Map;

/**
 * System module public API for authentication operations.
 */
public interface SystemAuthService {

    Map<String, Object> getCurrentUser();

    Map<String, Object> logout();
}
