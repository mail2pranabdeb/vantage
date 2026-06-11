package com.pd.common.aspect;

import com.pd.common.annotation.Log;
import com.pd.common.event.operation.OperationLogEvent;
import com.pd.common.util.EntityDiffUtil;
import com.pd.framework.config.AuditContextHolder;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.HashMap;
import java.util.Map;

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

    @Before("@annotation(logAnnotation)")
    public void before(JoinPoint joinPoint, Log logAnnotation) {
        if (isGetRequest()) return;
        startTimeHolder.set(System.currentTimeMillis());
        if (shouldCaptureBeforeState(logAnnotation)) {
            Object beforeEntity = extractEntityFromArgs(joinPoint);
            if (beforeEntity != null) beforeEntityHolder.set(beforeEntity);
        }
    }

    @AfterReturning(pointcut = "@annotation(logAnnotation)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Log logAnnotation, Object result) {
        if (isGetRequest()) return;
        if (shouldCaptureAfterState(logAnnotation)) {
            Object afterEntity = extractEntityFromResult(result);
            if (afterEntity != null) afterEntityHolder.set(afterEntity);
        }
        publishOperationLog(joinPoint, logAnnotation, result, null, 0);
        beforeEntityHolder.remove();
        afterEntityHolder.remove();
    }

    @AfterThrowing(pointcut = "@annotation(logAnnotation)", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Log logAnnotation, Exception e) {
        if (isGetRequest()) return;
        publishOperationLog(joinPoint, logAnnotation, null, e, 1);
        beforeEntityHolder.remove();
        afterEntityHolder.remove();
    }

    @Before("execution(* com.pd.gateway.GatewayManagement.*(..)) && !@annotation(com.pd.common.annotation.Log)")
    public void beforeDefault(JoinPoint joinPoint) {
        if (isGetRequest()) return;
        startTimeHolder.set(System.currentTimeMillis());
        Object beforeEntity = extractEntityFromArgs(joinPoint);
        if (beforeEntity != null) beforeEntityHolder.set(beforeEntity);
    }

    @AfterReturning(pointcut = "execution(* com.pd.gateway.GatewayManagement.*(..)) && !@annotation(com.pd.common.annotation.Log)", returning = "result")
    public void afterReturningDefault(JoinPoint joinPoint, Object result) {
        if (isGetRequest()) return;
        Object afterEntity = extractEntityFromResult(result);
        if (afterEntity != null) afterEntityHolder.set(afterEntity);
        publishOperationLog(joinPoint, null, result, null, 0);
        beforeEntityHolder.remove();
        afterEntityHolder.remove();
    }

    @AfterThrowing(pointcut = "execution(* com.pd.gateway.GatewayManagement.*(..)) && !@annotation(com.pd.common.annotation.Log)", throwing = "e")
    public void afterThrowingDefault(JoinPoint joinPoint, Exception e) {
        if (isGetRequest()) return;
        publishOperationLog(joinPoint, null, null, e, 1);
        beforeEntityHolder.remove();
        afterEntityHolder.remove();
    }

    private boolean isGetRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return "GET".equalsIgnoreCase(attributes.getRequest().getMethod());
        }
        return false;
    }

    private void publishOperationLog(JoinPoint joinPoint, Log logAnnotation, Object result, Exception e, int status) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;

            HttpServletRequest request = attributes.getRequest();
            Long startTime = startTimeHolder.get();
            startTimeHolder.remove();
            long costTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            String method = className + "." + methodName + "()";

            String oldValues = null;
            String newValues = null;
            String changedFields = null;

            Map<String, Object> auditBeforeMap = AuditContextHolder.getBeforeState();
            if (auditBeforeMap != null) {
                oldValues = AuditContextHolder.getBeforeStateJson();
                Object afterEntity = afterEntityHolder.get();
                if (afterEntity != null) {
                    newValues = EntityDiffUtil.serializeEntity(afterEntity);
                    changedFields = EntityDiffUtil.computeChangedFieldsFromMap(auditBeforeMap, afterEntity);
                } else {
                    Object resultEntity = extractEntityFromResult(result);
                    if (resultEntity != null) {
                        newValues = EntityDiffUtil.serializeEntity(resultEntity);
                        changedFields = EntityDiffUtil.computeChangedFieldsFromMap(auditBeforeMap, resultEntity);
                    }
                }
            }

            if (oldValues == null) {
                try {
                    Class<?> ctxClass = Class.forName("com.pd.modules.system.context.UserAuditContextHolder");
                    Object be = ctxClass.getMethod("getBeforeEntity").invoke(null);
                    Object ae = ctxClass.getMethod("getAfterEntity").invoke(null);
                    if (be != null || ae != null) {
                        oldValues = EntityDiffUtil.serializeEntity(be);
                        newValues = EntityDiffUtil.serializeEntity(ae);
                        changedFields = EntityDiffUtil.computeChangedFields(be, ae);
                    }
                } catch (ClassNotFoundException ignored) {
                    Object be = beforeEntityHolder.get();
                    Object ae = afterEntityHolder.get();
                    if (be != null || ae != null) {
                        oldValues = EntityDiffUtil.serializeEntity(be);
                        newValues = EntityDiffUtil.serializeEntity(ae);
                        changedFields = EntityDiffUtil.computeChangedFields(be, ae);
                    }
                } catch (Exception ex) {
                    Object be = beforeEntityHolder.get();
                    Object ae = afterEntityHolder.get();
                    if (be != null || ae != null) {
                        oldValues = EntityDiffUtil.serializeEntity(be);
                        newValues = EntityDiffUtil.serializeEntity(ae);
                        changedFields = EntityDiffUtil.computeChangedFields(be, ae);
                    }
                }
            }

            String jsonResult = "";
            boolean saveResponse = logAnnotation == null || logAnnotation.isSaveResponseData();
            if (saveResponse && result != null) {
                try {
                    jsonResult = objectMapper.writeValueAsString(result);
                } catch (Exception ex) {
                    jsonResult = result.toString();
                }
            }

            OperationLogEvent event = new OperationLogEvent(
                extractTitle(joinPoint, logAnnotation),
                logAnnotation != null ? logAnnotation.businessType().value() : guessBusinessType(request),
                method,
                request.getMethod(),
                logAnnotation != null ? logAnnotation.operatorType().value() : 1,
                getCurrentUser(),
                "",
                request.getRequestURI(),
                getClientIp(request),
                "",
                logAnnotation != null && logAnnotation.isSaveRequestData() ? paramsToString(joinPoint.getArgs()) : "",
                jsonResult,
                status,
                e != null ? e.getMessage() : "",
                costTime,
                oldValues,
                newValues,
                changedFields
            );

            log.info("=== Publishing OperationLogEvent: title={}, url={}, status={} ===",
                event.getTitle(), event.getOperUrl(), event.getStatus());
            eventPublisher.publishEvent(event);

            AuditContextHolder.clear();
            try {
                Class<?> ctxClass = Class.forName("com.pd.modules.system.context.UserAuditContextHolder");
                ctxClass.getMethod("clear").invoke(null);
            } catch (Exception ignored) {}

        } catch (Exception ex) {
            log.error("=== Failed to publish operation log event ===", ex);
        }
    }

    private int guessBusinessType(HttpServletRequest request) {
        return switch (request.getMethod().toUpperCase()) {
            case "POST" -> Log.BusinessType.INSERT.value();
            case "PUT" -> Log.BusinessType.UPDATE.value();
            case "DELETE" -> Log.BusinessType.DELETE.value();
            default -> Log.BusinessType.OTHER.value();
        };
    }

    private String extractTitle(JoinPoint joinPoint, Log logAnnotation) {
        if (logAnnotation != null && !logAnnotation.title().isEmpty()) {
            return logAnnotation.title();
        }
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }

    private String getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !(auth.getPrincipal() instanceof String)
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                Object principal = auth.getPrincipal();
                return (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();
            }
        } catch (Exception e) {
            log.debug("=== Failed to extract current user ===");
        }
        return "anonymous";
    }

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

    private String paramsToString(Object[] args) {
        if (args == null || args.length == 0) return "";
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
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(args[i] != null ? args[i].toString() : "null");
            }
            return sb.length() > 4000 ? sb.substring(0, 4000) + "..." : sb.toString();
        }
    }

    private boolean shouldCaptureBeforeState(Log logAnnotation) {
        int businessType = logAnnotation.businessType().value();
        return businessType == Log.BusinessType.UPDATE.value()
            || businessType == Log.BusinessType.DELETE.value();
    }

    private boolean shouldCaptureAfterState(Log logAnnotation) {
        int businessType = logAnnotation.businessType().value();
        return businessType == Log.BusinessType.INSERT.value()
            || businessType == Log.BusinessType.UPDATE.value();
    }

    private Object extractEntityFromArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) return null;
        for (Object arg : args) {
            if (arg != null && isEntityCandidate(arg)) return arg;
        }
        return null;
    }

    private Object extractEntityFromResult(Object result) {
        if (result == null) return null;
        try {
            if (result instanceof HashMap<?, ?> map) {
                Object data = map.get("data");
                if (data != null && isEntityCandidate(data)) return data;
            }
        } catch (Exception e) {
            log.debug("=== Could not extract entity from result wrapper ===");
        }
        if (isEntityCandidate(result)) return result;
        return null;
    }

    private boolean isEntityCandidate(Object obj) {
        if (obj == null) return false;
        String className = obj.getClass().getName();
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
