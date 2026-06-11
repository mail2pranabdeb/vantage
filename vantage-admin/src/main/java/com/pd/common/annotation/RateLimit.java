package com.pd.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    String key() default "";
    int capacity() default 20;
    int duration() default 60;
    boolean perUser() default true;
}
