package com.pd.common.annotation;

import java.lang.annotation.*;

/**
 * Custom annotation for operation logging.
 * Apply this annotation to methods that should be logged.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    
    /**
     * Module title for the operation
     */
    String title() default "";
    
    /**
     * Business type (0=other, 1=insert, 2=update, 3=delete, 4=grant, 5=export, 6=import, 7=force, 8=gen, 9=clean)
     */
    BusinessType businessType() default BusinessType.OTHER;
    
    /**
     * Operator type (0=other, 1=admin, 2=mobile)
     */
    OperatorType operatorType() default OperatorType.WEB;
    
    /**
     * Whether to save request parameters
     */
    boolean isSaveRequestData() default true;
    
    /**
     * Whether to save response data
     */
    boolean isSaveResponseData() default true;
    
    /**
     * Whether to log error messages
     */
    boolean isLogError() default true;
    
    /**
     * Business types
     */
    enum BusinessType {
        OTHER(0),
        INSERT(1),
        UPDATE(2),
        DELETE(3),
        GRANT(4),
        EXPORT(5),
        IMPORT(6),
        FORCE(7),
        GEN(8),
        CLEAN(9);
        
        private final int value;
        
        BusinessType(int value) {
            this.value = value;
        }
        
        public int value() {
            return value;
        }
    }
    
    /**
     * Operator types
     */
    enum OperatorType {
        OTHER(0),
        WEB(1),
        MOBILE(2);
        
        private final int value;
        
        OperatorType(int value) {
            this.value = value;
        }
        
        public int value() {
            return value;
        }
    }
}
