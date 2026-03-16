package com.pd.common.aspect;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.event.operation.OperationLogEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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
 * Aspect for handling @Log annotation.
 * Captures method execution and publishes OperationLogEvent.
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    private final ApplicationEventPublisher eventPublisher;
    private final ThreadLocal<Long> startTimeHolder = new ThreadLocal<>();

    public LogAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Pointcut for methods annotated with @Log
     */
    @Before("@annotation(logAnnotation)")
    public void before(JoinPoint joinPoint, Log logAnnotation) {
        startTimeHolder.set(System.currentTimeMillis());
        log.debug("Starting logged method: {}", joinPoint.getSignature().toShortString());
    }

    /**
     * Handle successful method execution
     */
    @AfterReturning(pointcut = "@annotation(logAnnotation)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Log logAnnotation, Object result) {
        publishOperationLog(joinPoint, logAnnotation, result, null, 0);
    }

    /**
     * Handle method execution with exception
     */
    @AfterThrowing(pointcut = "@annotation(logAnnotation)", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Log logAnnotation, Exception e) {
        if (logAnnotation.isLogError()) {
            publishOperationLog(joinPoint, logAnnotation, null, e, 1);
        }
    }

    /**
     * Publish operation log event
     */
    private void publishOperationLog(JoinPoint joinPoint, Log logAnnotation, Object result, Exception e, int status) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            long costTime = System.currentTimeMillis() - startTimeHolder.get();
            startTimeHolder.remove();

            // Generate trace IDs
            String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

            // Extract method info
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            String method = className + "." + methodName + "()";

            // Build operation log event
            OperationLogEvent event = new OperationLogEvent(
                logAnnotation.title(),                          // title
                logAnnotation.businessType().value(),           // businessType
                method,                                         // method
                request.getMethod(),                            // requestMethod
                logAnnotation.operatorType().value(),           // operatorType
                getCurrentUser(),                               // operName
                "",                                             // deptName
                request.getRequestURI(),                        // operUrl
                getClientIp(request),                           // operIp
                "",                                             // operLocation
                logAnnotation.isSaveRequestData() ? paramsToString(joinPoint.getArgs()) : "", // operParam
                (logAnnotation.isSaveResponseData() && result != null) ? result.toString() : "", // jsonResult
                status,                                         // status
                e != null ? e.getMessage() : "",                // errorMsg
                costTime                                        // costTime
            );

            eventPublisher.publishEvent(event);

        } catch (Exception ex) {
            log.error("Failed to publish operation log event", ex);
        }
    }

    /**
     * Get current logged in user from SecurityContext
     */
    private String getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                    && !(authentication.getPrincipal() instanceof String)
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    return ((UserDetails) principal).getUsername();
                }
                return principal.toString();
            }
        } catch (Exception e) {
            log.warn("Failed to get current user", e);
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
