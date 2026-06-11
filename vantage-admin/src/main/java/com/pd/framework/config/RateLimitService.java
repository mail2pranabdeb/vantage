package com.pd.framework.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private static class Bucket {
        final long capacity;
        final double refillPerSecond;
        volatile double tokens;
        volatile long lastRefillNanos;

        Bucket(long capacity, int durationSeconds) {
            this.capacity = capacity;
            this.refillPerSecond = (double) capacity / durationSeconds;
            this.tokens = capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        void refill() {
            long now = System.nanoTime();
            double elapsed = (now - lastRefillNanos) / 1_000_000_000.0;
            tokens = Math.min(capacity, tokens + elapsed * refillPerSecond);
            lastRefillNanos = now;
        }

        int remaining() {
            refill();
            return (int) Math.floor(tokens);
        }
    }

    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong blockedRequests = new AtomicLong(0);

    public boolean tryAcquire(String key, int capacity, int duration) {
        totalRequests.incrementAndGet();
        Bucket bucket = bucketCache.get(key, k -> new Bucket(capacity, duration));
        if (bucket == null) return false;
        boolean allowed = bucket.tryConsume();
        if (!allowed) {
            blockedRequests.incrementAndGet();
            log.debug("Rate limit exceeded for key: {}", key);
        }
        return allowed;
    }

    public int remaining(String key) {
        Bucket bucket = bucketCache.getIfPresent(key);
        return bucket != null ? bucket.remaining() : 0;
    }

    public long getTotalRequests() { return totalRequests.get(); }
    public long getBlockedRequests() { return blockedRequests.get(); }
    public long getActiveKeys() { return bucketCache.estimatedSize(); }
}
