package com.pd.common.aspect;

import com.pd.common.annotation.RateLimit;
import com.pd.framework.config.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    private final RateLimitService rateLimitService;

    public RateLimitAspect(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        HttpServletRequest request = getRequest();
        if (request == null) return;

        String key = buildKey(joinPoint, rateLimit, request);
        boolean allowed = rateLimitService.tryAcquire(key, rateLimit.capacity(), rateLimit.duration());

        setResponseHeaders(key, rateLimit);

        if (!allowed) {
            log.warn("Rate limit exceeded: key={}, ip={}", key, request.getRemoteAddr());
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded. Try again in " + rateLimit.duration() + " seconds.");
        }
    }

    private String buildKey(JoinPoint joinPoint, RateLimit rateLimit, HttpServletRequest request) {
        if (!rateLimit.key().isEmpty()) return rateLimit.key();
        String method = joinPoint.getSignature().toShortString();
        if (rateLimit.perUser()) {
            String user = getCurrentUsername();
            return "rate:" + method + ":user:" + (user != null ? user : "anonymous");
        }
        return "rate:" + method + ":ip:" + request.getRemoteAddr();
    }

    private String getCurrentUsername() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "anonymous";
    }

    private void setResponseHeaders(String key, RateLimit rateLimit) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletResponse response = attrs.getResponse();
            if (response != null && !response.isCommitted()) {
                response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimit.capacity()));
                response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitService.remaining(key)));
                response.setHeader("X-RateLimit-Reset", String.valueOf(rateLimit.duration()));
            }
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
