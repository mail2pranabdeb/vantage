package com.pd.modules.system.aspect;

import com.pd.common.annotation.Audit;
import com.pd.modules.system.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect for automatic audit logging
 * Intercepts methods annotated with @Audit and logs audit trails
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Around advice for audit logging
     */
    @Around("@annotation(com.pd.common.annotation.Audit)")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get method annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Audit audit = method.getAnnotation(Audit.class);

        if (audit == null) {
            return joinPoint.proceed();
        }

        String tableName = audit.tableName();
        String module = audit.module();
        String operation = audit.operation();

        // Get object before operation (for UPDATE/DELETE)
        Object beforeObject = getTargetObject(joinPoint);

        // Execute method
        Object result = joinPoint.proceed();

        // Get object after operation (for INSERT/UPDATE)
        Object afterObject = getTargetObject(joinPoint);

        // Determine operation type if not specified
        if (operation.isEmpty()) {
            operation = determineOperationType(method.getName(), beforeObject, afterObject);
        }

        // Get record ID
        Long recordId = getRecordId(afterObject != null ? afterObject : beforeObject);

        // Log audit
        try {
            auditLogService.logAudit(
                tableName,
                recordId,
                operation,
                beforeObject,
                afterObject,
                module
            );
        } catch (Exception e) {
            log.error("Failed to log audit for {}.{}", tableName, recordId, e);
        }

        return result;
    }

    /**
     * Get target object from join point arguments
     */
    private Object getTargetObject(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            // Return first non-primitive argument
            for (Object arg : args) {
                if (arg != null && !isPrimitive(arg.getClass())) {
                    return arg;
                }
            }
        }
        return null;
    }

    /**
     * Extract record ID from object
     */
    private Long getRecordId(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            // Try to get ID using reflection
            java.lang.reflect.Method getIdMethod = obj.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(obj);
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
        } catch (Exception e) {
            // Ignore - ID extraction failed
        }

        return null;
    }

    /**
     * Determine operation type based on method name
     */
    private String determineOperationType(String methodName, Object before, Object after) {
        String name = methodName.toLowerCase();
        
        if (name.contains("save") || name.contains("insert") || name.contains("create") || name.contains("add")) {
            return before == null && after != null ? "INSERT" : "UPDATE";
        }
        if (name.contains("delete") || name.contains("remove")) {
            return "DELETE";
        }
        if (name.contains("update") || name.contains("edit") || name.contains("modify")) {
            return "UPDATE";
        }
        
        return "UNKNOWN";
    }

    /**
     * Check if class is primitive or wrapper
     */
    private boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz == String.class ||
               clazz == Boolean.class ||
               clazz == Character.class ||
               Number.class.isAssignableFrom(clazz);
    }
}
