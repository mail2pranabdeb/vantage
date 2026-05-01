package com.pd.modules.system.api;

import java.util.List;
import java.util.Map;

/**
 * System module public API for cache management.
 */
public interface SystemCacheService {

    List<Map<String, Object>> listCaches();

    String clearCache(String cacheName);

    String clearAllCaches();

    Map<String, Object> getCacheStats(String cacheName);
}
