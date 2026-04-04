package com.pd.modules.system.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local context holder for user audit trail.
 * Allows service layer to pass before/after entity states to the AOP logging aspect.
 * Entities are deep-cloned via JSON to detach them from JPA persistence context.
 */
public class UserAuditContextHolder {
    
    private static final Logger log = LoggerFactory.getLogger(UserAuditContextHolder.class);
    private static final ThreadLocal<Object> beforeEntityHolder = new ThreadLocal<>();
    private static final ThreadLocal<Object> afterEntityHolder = new ThreadLocal<>();
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * Deep-clone an entity to detach it from JPA persistence context.
     * This ensures the "before" state doesn't get modified by JPA dirty checking.
     */
    @SuppressWarnings("unchecked")
    private static <T> T detachEntity(T entity) {
        if (entity == null) return null;
        try {
            String json = objectMapper.writeValueAsString(entity);
            return (T) objectMapper.readValue(json, entity.getClass());
        } catch (Exception e) {
            log.warn("=== Failed to detach entity: {} ===", e.getMessage());
            return entity;
        }
    }

    public static void setBeforeEntity(Object entity) {
        beforeEntityHolder.set(detachEntity(entity));
    }

    public static Object getBeforeEntity() {
        return beforeEntityHolder.get();
    }

    public static void setAfterEntity(Object entity) {
        afterEntityHolder.set(detachEntity(entity));
    }

    public static Object getAfterEntity() {
        return afterEntityHolder.get();
    }

    public static void clear() {
        beforeEntityHolder.remove();
        afterEntityHolder.remove();
    }
}
