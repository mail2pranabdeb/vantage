package com.pd.common.annotation;

import java.lang.annotation.*;

/**
 * Annotation for audit logging
 * Add this annotation to service methods to automatically log audit trails
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {
    
    /**
     * Table name being audited
     */
    String tableName();
    
    /**
     * Module name (e.g., "User Management", "Job Scheduling")
     */
    String module() default "";
    
    /**
     * Custom operation name (default: INSERT/UPDATE/DELETE based on method)
     */
    String operation() default "";
}
