package com.pd.modules.system.controller;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Cache management controller
 */
@RestController
@RequestMapping("/api/system/cache")
public class CacheController extends BaseController {

    @Autowired
    private CacheManager cacheManager;

    /**
     * Get all cache names and their sizes
     */
    @GetMapping("/list")
    public AjaxResult list() {
        List<Map<String, Object>> caches = new ArrayList<>();
        
        if (cacheManager != null) {
            for (String cacheName : cacheManager.getCacheNames()) {
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("cacheName", cacheName);
                
                try {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cacheInfo.put("type", cache.getClass().getSimpleName());
                        cacheInfo.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                    }
                } catch (Exception e) {
                    cacheInfo.put("error", e.getMessage());
                }
                
                caches.add(cacheInfo);
            }
        }
        
        return success(caches);
    }

    /**
     * Get cache keys
     */
    @GetMapping("/keys/{cacheName}")
    public AjaxResult getKeys(@PathVariable String cacheName) {
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                return error("Cache not found: " + cacheName);
            }
            
            // Note: Caffeine doesn't expose keys directly, this is a limitation
            Map<String, Object> result = new HashMap<>();
            result.put("cacheName", cacheName);
            result.put("message", "Direct key enumeration not supported for this cache type");
            result.put("type", cache.getClass().getSimpleName());
            
            return success(result);
        } catch (Exception e) {
            return error("Failed to get cache keys: " + e.getMessage());
        }
    }

    /**
     * Clear specific cache
     */
    @PostMapping("/clear/{cacheName}")
    public AjaxResult clear(@PathVariable String cacheName) {
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                return error("Cache not found: " + cacheName);
            }
            
            cache.clear();
            return success("Cache '" + cacheName + "' cleared successfully");
        } catch (Exception e) {
            return error("Failed to clear cache: " + e.getMessage());
        }
    }

    /**
     * Clear all caches
     */
    @PostMapping("/clear-all")
    public AjaxResult clearAll() {
        try {
            if (cacheManager != null) {
                for (String cacheName : cacheManager.getCacheNames()) {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                    }
                }
            }
            return success("All caches cleared successfully");
        } catch (Exception e) {
            return error("Failed to clear all caches: " + e.getMessage());
        }
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/stats/{cacheName}")
    public AjaxResult getStats(@PathVariable String cacheName) {
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                return error("Cache not found: " + cacheName);
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("cacheName", cacheName);
            stats.put("type", cache.getClass().getSimpleName());
            stats.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
            
            // Caffeine specific stats (if available)
            try {
                Object nativeCache = cache.getNativeCache();
                if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                    com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = 
                        (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache;
                    stats.put("estimatedSize", caffeineCache.estimatedSize());
                }
            } catch (Exception e) {
                // Ignore if not Caffeine cache
            }
            
            return success(stats);
        } catch (Exception e) {
            return error("Failed to get cache stats: " + e.getMessage());
        }
    }
}
