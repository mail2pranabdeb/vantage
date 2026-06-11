package com.pd.framework.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuditContextHolder {

    private static final Logger log = LoggerFactory.getLogger(AuditContextHolder.class);
    private static final ObjectMapper objectMapper;
    private static final ThreadLocal<Map<String, Object>> beforeStateHolder = new ThreadLocal<>();

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public static void setBeforeState(Map<String, Object> state) {
        beforeStateHolder.set(state);
    }

    public static Map<String, Object> getBeforeState() {
        return beforeStateHolder.get();
    }

    public static String getBeforeStateJson() {
        Map<String, Object> state = beforeStateHolder.get();
        if (state == null) return null;
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit before-state", e);
            return null;
        }
    }

    public static void clear() {
        beforeStateHolder.remove();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildStateMap(Object entity) {
        if (entity == null) return null;
        try {
            return objectMapper.convertValue(entity, LinkedHashMap.class);
        } catch (Exception e) {
            log.warn("Failed to build state map for audit", e);
            return null;
        }
    }
}
