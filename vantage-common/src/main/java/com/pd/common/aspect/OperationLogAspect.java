package com.pd.common.aspect;

import com.pd.common.annotation.Log;
import com.pd.common.event.operation.OperationLogEvent;
import com.pd.common.util.EntityDiffUtil;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.HashMap;
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
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    private final ApplicationEventPublisher eventPublisher;
    private final ThreadLocal<Long> startTimeHolder = new ThreadLocal<>();
    private final ThreadLocal<Object> beforeEntityHolder = new ThreadLocal<>();
    private final ThreadLocal<Object> afterEntityHolder = new ThreadLocal<>();

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
        
        // Capture entity state BEFORE modification for UPDATE/DELETE operations
        if (shouldCaptureBeforeState(logAnnotation)) {
            Object beforeEntity = extractEntityFromArgs(joinPoint);
            if (beforeEntity != null) {
                beforeEntityHolder.set(beforeEntity);
                log.debug("=== Captured before-state entity for: {} ===", joinPoint.getSignature().toShortString());
            }
        }
        
        log.info("=== AOP BEFORE: {} ===", joinPoint.getSignature().toShortString());
    }

    /**
     * Handle successful method execution
     */
    @AfterReturning(pointcut = "@annotation(logAnnotation)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Log logAnnotation, Object result) {
        if (!isGetRequest()) {
            log.info("=== @AfterReturning TRIGGERED for: {} ===", joinPoint.getSignature().toShortString());
            
            // Capture entity state AFTER modification for INSERT/UPDATE operations
            Object afterEntity = null;
            if (shouldCaptureAfterState(logAnnotation)) {
                afterEntity = extractEntityFromResult(result);
                if (afterEntity != null) {
                    afterEntityHolder.set(afterEntity);
                    log.debug("=== Captured after-state entity for: {} ===", joinPoint.getSignature().toShortString());
                }
            }
            
            publishOperationLog(joinPoint, logAnnotation, result, null, 0);
            
            // Clean up ThreadLocals
            beforeEntityHolder.remove();
            afterEntityHolder.remove();
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
            
            // Clean up ThreadLocals
            beforeEntityHolder.remove();
            afterEntityHolder.remove();
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

            // Compute before/after values for audit trail
            String oldValues = null;
            String newValues = null;
            String changedFields = null;

            // First, try to get from service-layer context (preferred)
            Object beforeEntity = null;
            Object afterEntity = null;
            
            try {
                Class<?> contextHolderClass = Class.forName("com.pd.modules.system.context.UserAuditContextHolder");
                beforeEntity = contextHolderClass.getMethod("getBeforeEntity").invoke(null);
                afterEntity = contextHolderClass.getMethod("getAfterEntity").invoke(null);
            } catch (ClassNotFoundException ex) {
                // UserAuditContextHolder not found, fall back to AOP-captured state
                beforeEntity = beforeEntityHolder.get();
                afterEntity = afterEntityHolder.get();
            } catch (Exception ex) {
                log.debug("=== Failed to read from UserAuditContextHolder, falling back ===", ex);
                beforeEntity = beforeEntityHolder.get();
                afterEntity = afterEntityHolder.get();
            }
            
            // Serialize if we have data
            if (beforeEntity != null || afterEntity != null) {
                oldValues = EntityDiffUtil.serializeEntity(beforeEntity);
                newValues = EntityDiffUtil.serializeEntity(afterEntity);
                changedFields = EntityDiffUtil.computeChangedFields(beforeEntity, afterEntity);
                log.debug("=== Computed audit trail: oldValues={}, newValues={}, changedFields={} ===",
                    oldValues != null ? oldValues.length() : 0,
                    newValues != null ? newValues.length() : 0,
                    changedFields);
            }

            // Build operation log event
            String jsonResult = "";
            if (logAnnotation.isSaveResponseData() && result != null) {
                try {
                    jsonResult = objectMapper.writeValueAsString(result);
                } catch (Exception ex) {
                    jsonResult = result.toString();
                }
            }

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
                jsonResult,                                       // jsonResult
                status,                                           // status
                e != null ? e.getMessage() : "",                  // errorMsg
                costTime,                                         // costTime
                oldValues,                                        // oldValues
                newValues,                                        // newValues
                changedFields                                     // changedFields
            );

            log.info("=== Publishing OperationLogEvent ===");
            eventPublisher.publishEvent(event);
            log.info("=== OperationLogEvent published successfully ===");

            // Clean up service-layer context holders
            try {
                Class<?> contextHolderClass = Class.forName("com.pd.modules.system.context.UserAuditContextHolder");
                contextHolderClass.getMethod("clear").invoke(null);
            } catch (Exception ex) {
                // Ignore - context holder may not exist
            }

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
     * Convert parameters to JSON string
     */
    private String paramsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                if (args[i] != null) {
                    sb.append(objectMapper.writeValueAsString(args[i]));
                } else {
                    sb.append("null");
                }
            }
            return sb.length() > 4000 ? sb.substring(0, 4000) + "..." : sb.toString();
        } catch (Exception ex) {
            // Fallback to toString() if JSON serialization fails
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(args[i] != null ? args[i].toString() : "null");
            }
            return sb.length() > 4000 ? sb.substring(0, 4000) + "..." : sb.toString();
        }
    }

    /**
     * Determine if we should capture entity state BEFORE modification.
     * Returns true for UPDATE and DELETE operations.
     */
    private boolean shouldCaptureBeforeState(Log logAnnotation) {
        int businessType = logAnnotation.businessType().value();
        return businessType == Log.BusinessType.UPDATE.value() 
            || businessType == Log.BusinessType.DELETE.value();
    }

    /**
     * Determine if we should capture entity state AFTER modification.
     * Returns true for INSERT and UPDATE operations.
     */
    private boolean shouldCaptureAfterState(Log logAnnotation) {
        int businessType = logAnnotation.businessType().value();
        return businessType == Log.BusinessType.INSERT.value() 
            || businessType == Log.BusinessType.UPDATE.value();
    }

    /**
     * Extract entity from method arguments.
     * Looks for the first non-primitive, non-exception argument that is not a standard web type.
     */
    private Object extractEntityFromArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg != null && isEntityCandidate(arg)) {
                return arg;
            }
        }
        return null;
    }

    /**
     * Extract entity from method result.
     * For operations that return AjaxResult, tries to extract the data field.
     * Otherwise returns the result itself if it's an entity candidate.
     */
    private Object extractEntityFromResult(Object result) {
        if (result == null) {
            return null;
        }
        // Try to extract entity from common response wrappers
        try {
            // If result is AjaxResult (extends HashMap), try to get data field
            if (result instanceof HashMap) {
                HashMap<?, ?> map = (HashMap<?, ?>) result;
                Object data = map.get("data");
                if (data != null && isEntityCandidate(data)) {
                    return data;
                }
            }
        } catch (Exception e) {
            log.debug("=== Could not extract entity from result wrapper ===");
        }

        // If result itself is an entity candidate, return it
        if (isEntityCandidate(result)) {
            return result;
        }
        return null;
    }

    /**
     * Check if an object is a candidate for entity logging.
     * Excludes primitive types, strings, exceptions, and common web types.
     */
    private boolean isEntityCandidate(Object obj) {
        if (obj == null) {
            return false;
        }
        String className = obj.getClass().getName();
        // Exclude common non-entity types
        return !className.startsWith("java.lang.") 
            && !className.startsWith("jakarta.")
            && !className.startsWith("javax.")
            && !className.startsWith("org.springframework.")
            && !(obj instanceof Exception)
            && !(obj instanceof String)
            && !(obj instanceof Number)
            && !(obj instanceof Boolean);
    }
}
