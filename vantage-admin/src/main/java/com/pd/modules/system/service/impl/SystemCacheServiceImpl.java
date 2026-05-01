package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemCacheService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SystemCacheServiceImpl implements SystemCacheService {

    private final CacheManager cacheManager;

    public SystemCacheServiceImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public List<Map<String, Object>> listCaches() {
        List<Map<String, Object>> caches = new ArrayList<>();
        if (cacheManager != null) {
            for (String cacheName : cacheManager.getCacheNames()) {
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("cacheName", cacheName);
                try {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cacheInfo.put("type", cache.getClass().getSimpleName());
                        cacheInfo.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                        try {
                            Object nativeCache = cache.getNativeCache();
                            if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache =
                                        (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache;
                                cacheInfo.put("size", (int) caffeineCache.estimatedSize());
                            } else {
                                cacheInfo.put("size", 0);
                            }
                        } catch (Exception e) {
                            cacheInfo.put("size", 0);
                        }
                    }
                } catch (Exception e) {
                    cacheInfo.put("error", e.getMessage());
                    cacheInfo.put("size", 0);
                }
                caches.add(cacheInfo);
            }
        }
        return caches;
    }

    @Override
    public String clearCache(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                return "Cache not found: " + cacheName;
            }
            cache.clear();
            return "Cache '" + cacheName + "' cleared successfully";
        } catch (Exception e) {
            return "Failed to clear cache: " + e.getMessage();
        }
    }

    @Override
    public String clearAllCaches() {
        try {
            if (cacheManager != null) {
                for (String cacheName : cacheManager.getCacheNames()) {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                    }
                }
            }
            return "All caches cleared successfully";
        } catch (Exception e) {
            return "Failed to clear all caches: " + e.getMessage();
        }
    }

    @Override
    public Map<String, Object> getCacheStats(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                throw new RuntimeException("Cache not found: " + cacheName);
            }
            Map<String, Object> stats = new HashMap<>();
            stats.put("cacheName", cacheName);
            stats.put("type", cache.getClass().getSimpleName());
            stats.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
            try {
                Object nativeCache = cache.getNativeCache();
                if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                    com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache =
                            (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache;
                    stats.put("size", (int) caffeineCache.estimatedSize());
                }
            } catch (Exception e) {
                stats.put("size", 0);
            }
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get cache stats: " + e.getMessage());
        }
    }
}
