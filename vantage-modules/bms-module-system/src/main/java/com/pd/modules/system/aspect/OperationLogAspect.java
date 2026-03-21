package com.pd.modules.system.aspect;

import com.pd.common.event.operation.OperationLogEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Aspect for automatic operation logging.
 * Captures controller method executions and publishes OperationLogEvent.
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
     * Pointcut for methods with @Log annotation
     */
    @Pointcut("@annotation(com.pd.common.annotation.Log)")
    public void logPointcut() {
        log.info("=== AOP Pointcut matched for @Log annotation ===");
    }

    /**
     * Record start time before method execution
     */
    @Before("logPointcut()")
    public void before(JoinPoint joinPoint) {
        startTimeHolder.set(System.currentTimeMillis());
        log.info("=== AOP BEFORE: {} ===", joinPoint.getSignature().toShortString());
    }

    /**
     * Publish operation log event after successful method execution
     */
    @AfterReturning(pointcut = "logPointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        log.info("=== @AfterReturning TRIGGERED for: {} ===", joinPoint.getSignature().toShortString());
        publishOperationLog(joinPoint, result, null, 0);
    }

    /**
     * Publish operation log event after method throws exception
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        log.info("=== @AfterThrowing TRIGGERED for: {} ===", joinPoint.getSignature().toShortString());
        publishOperationLog(joinPoint, null, e, 1);
    }

    /**
     * Publish operation log event
     */
    private void publishOperationLog(JoinPoint joinPoint, Object result, Exception e, int status) {
        log.info("=== publishOperationLog called ===");
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
                extractTitle(joinPoint), method, request.getRequestURI());

            // Build operation log event
            OperationLogEvent event = new OperationLogEvent(
                extractTitle(joinPoint),                          // title
                extractBusinessType(joinPoint),                   // businessType
                method,                                           // method
                request.getMethod(),                              // requestMethod
                0,                                                // operatorType (0=other)
                getCurrentUser(),                                 // operName
                getCurrentDept(),                                 // deptName
                request.getRequestURI(),                          // operUrl
                getClientIp(request),                             // operIp
                "",                                               // operLocation
                paramsToString(joinPoint.getArgs()),              // operParam
                result != null ? result.toString() : "",          // jsonResult
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
     * Extract title from request URI
     */
    private String extractTitle(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }

    /**
     * Extract business type from method name
     */
    private Integer extractBusinessType(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        if (methodName.contains("add") || methodName.contains("insert") || methodName.contains("create")) {
            return 1; // Insert
        } else if (methodName.contains("update") || methodName.contains("edit")) {
            return 2; // Update
        } else if (methodName.contains("delete") || methodName.contains("remove")) {
            return 3; // Delete
        }
        return 0; // Other
    }

    /**
     * Get current logged in user (placeholder)
     */
    private String getCurrentUser() {
        // TODO: Extract from SecurityContext
        return "anonymous";
    }

    /**
     * Get current department (placeholder)
     */
    private String getCurrentDept() {
        // TODO: Extract from user info
        return "";
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
        return sb.length() > 2000 ? sb.substring(0, 2000) + "..." : sb.toString();
    }
}
