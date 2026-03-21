package com.pd.common.aspect;

import com.pd.common.annotation.Log;
import com.pd.common.event.operation.OperationLogEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Aspect for automatic operation logging.
 * Captures controller method executions annotated with @Log and publishes OperationLogEvent.
 * 
 * Features:
 * - Only logs methods with @Log annotation (opt-in)
 * - Skips GET requests automatically
 * - Extracts title from @Log annotation
 * - Extracts current user from SecurityContext
 * - Generates trace IDs for log correlation
 * - Async event publishing for better performance
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final ApplicationEventPublisher eventPublisher;
    private final ThreadLocal<Long> startTimeHolder = new ThreadLocal<>();

    public OperationLogAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Pointcut for methods annotated with @Log
     */
    @Before("@annotation(logAnnotation)")
    public void before(JoinPoint joinPoint, Log logAnnotation) {
        // Skip GET requests - only log write operations
        if (isGetRequest()) {
            log.debug("=== Skipping operation log for GET request: {} ===", 
                joinPoint.getSignature().toShortString());
            return;
        }
        startTimeHolder.set(System.currentTimeMillis());
        log.info("=== AOP BEFORE: {} ===", joinPoint.getSignature().toShortString());
    }

    /**
     * Handle successful method execution
     */
    @AfterReturning(pointcut = "@annotation(logAnnotation)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Log logAnnotation, Object result) {
        if (!isGetRequest()) {
            log.info("=== @AfterReturning TRIGGERED for: {} ===", joinPoint.getSignature().toShortString());
            publishOperationLog(joinPoint, logAnnotation, result, null, 0);
        }
    }

    /**
     * Handle method execution with exception
     */
    @AfterThrowing(pointcut = "@annotation(logAnnotation)", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Log logAnnotation, Exception e) {
        if (!isGetRequest() && logAnnotation.isLogError()) {
            log.info("=== @AfterThrowing TRIGGERED for: {} ===", joinPoint.getSignature().toShortString());
            publishOperationLog(joinPoint, logAnnotation, null, e, 1);
        }
    }

    /**
     * Check if current request is GET
     */
    private boolean isGetRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String method = attributes.getRequest().getMethod();
            return "GET".equalsIgnoreCase(method);
        }
        return false;
    }

    /**
     * Publish operation log event
     */
    private void publishOperationLog(JoinPoint joinPoint, Log logAnnotation, Object result, Exception e, int status) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("=== No request attributes, skipping operation log ===");
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            long costTime = System.currentTimeMillis() - startTimeHolder.get();
            startTimeHolder.remove();

            // Extract method info
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            String method = className + "." + methodName + "()";

            log.info("=== Building OperationLogEvent: title={}, method={}, url={} ===",
                extractTitle(joinPoint, logAnnotation), method, request.getRequestURI());

            // Build operation log event
            OperationLogEvent event = new OperationLogEvent(
                extractTitle(joinPoint, logAnnotation),           // title
                logAnnotation.businessType().value(),             // businessType
                method,                                           // method
                request.getMethod(),                              // requestMethod
                logAnnotation.operatorType().value(),             // operatorType
                getCurrentUser(),                                 // operName
                "",                                               // deptName
                request.getRequestURI(),                          // operUrl
                getClientIp(request),                             // operIp
                "",                                               // operLocation
                logAnnotation.isSaveRequestData() ? paramsToString(joinPoint.getArgs()) : "", // operParam
                (logAnnotation.isSaveResponseData() && result != null) ? result.toString() : "", // jsonResult
                status,                                           // status
                e != null ? e.getMessage() : "",                  // errorMsg
                costTime                                          // costTime
            );

            log.info("=== Publishing OperationLogEvent ===");
            eventPublisher.publishEvent(event);
            log.info("=== OperationLogEvent published successfully ===");

        } catch (Exception ex) {
            log.error("=== Failed to publish operation log event ===", ex);
        }
    }

    /**
     * Extract title from @Log annotation
     */
    private String extractTitle(JoinPoint joinPoint, Log logAnnotation) {
        // Use title from @Log annotation if present
        if (logAnnotation != null && !logAnnotation.title().isEmpty()) {
            return logAnnotation.title();
        }
        // Fallback to class.method name
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }

    /**
     * Get current logged in user from SecurityContext
     */
    private String getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !(auth.getPrincipal() instanceof String)
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                Object principal = auth.getPrincipal();
                if (principal instanceof UserDetails) {
                    return ((UserDetails) principal).getUsername();
                }
                return principal.toString();
            }
        } catch (Exception e) {
            log.debug("=== Failed to extract current user ===");
        }
        return "anonymous";
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Convert parameters to string
     */
    private String paramsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(args[i] != null ? args[i].toString() : "null");
        }
        return sb.length() > 4000 ? sb.substring(0, 4000) + "..." : sb.toString();
    }
}
