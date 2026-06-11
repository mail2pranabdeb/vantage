package com.pd.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility for capturing entity state and computing diffs between before/after states.
 * Used for operation log audit trail.
 */
public class EntityDiffUtil {

    private static final Logger log = LoggerFactory.getLogger(EntityDiffUtil.class);
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public static class FieldDiff {
        public String field;
        public Object oldValue;
        public Object newValue;

        public FieldDiff(String field, Object oldValue, Object newValue) {
            this.field = field;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    public static String computeFieldDiffJson(Map<String, Object> beforeMap, Object afterEntity) {
        if (beforeMap == null && afterEntity == null) return "{}";
        if (afterEntity == null) {
            try { return objectMapper.writeValueAsString(beforeMap); } catch (Exception e) { return "{}"; }
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> afterMap = objectMapper.convertValue(afterEntity, Map.class);

            Map<String, FieldDiff> diffs = new LinkedHashMap<>();
            Set<String> allKeys = new HashSet<>();
            if (beforeMap != null) allKeys.addAll(beforeMap.keySet());
            if (afterMap != null) allKeys.addAll(afterMap.keySet());

            for (String key : allKeys) {
                Object beforeVal = beforeMap != null ? beforeMap.get(key) : null;
                Object afterVal = afterMap != null ? afterMap.get(key) : null;
                if (!Objects.equals(beforeVal, afterVal)) {
                    diffs.put(key, new FieldDiff(key, beforeVal, afterVal));
                }
            }

            List<Map<String, Object>> diffList = diffs.values().stream().map(d -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("field", d.field);
                m.put("oldValue", d.oldValue);
                m.put("newValue", d.newValue);
                return m;
            }).collect(Collectors.toList());

            return objectMapper.writeValueAsString(diffList);
        } catch (Exception e) {
            log.warn("Failed to compute field diff JSON", e);
            return "{}";
        }
    }

    public static String computeChangedFieldsFromMap(Map<String, Object> beforeMap, Object afterEntity) {
        if (beforeMap == null && afterEntity == null) return "";
        if (beforeMap == null) return "all";
        if (afterEntity == null) return "all";
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> afterMap = objectMapper.convertValue(afterEntity, Map.class);
            List<String> changedFields = new ArrayList<>();
            Set<String> allKeys = new HashSet<>(beforeMap.keySet());
            if (afterMap != null) allKeys.addAll(afterMap.keySet());
            for (String key : allKeys) {
                Object beforeValue = beforeMap.get(key);
                Object afterValue = afterMap != null ? afterMap.get(key) : null;
                if (!Objects.equals(beforeValue, afterValue)) {
                    changedFields.add(key);
                }
            }
            return String.join(",", changedFields);
        } catch (Exception e) {
            log.warn("Failed to compute changed fields from map", e);
            return "";
        }
    }

    /**
     * Serialize an entity to JSON string for storage in operation log.
     * Returns null if entity is null or serialization fails.
     */
    public static String serializeEntity(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (Exception e) {
            log.warn("Failed to serialize entity for operation log", e);
            return null;
        }
    }

    /**
     * Compute diff between two entity states and return changed field names.
     * Returns comma-separated field names that changed, or empty string if no changes.
     */
    public static String computeChangedFields(Object before, Object after) {
        if (before == null && after == null) {
            return "";
        }
        if (before == null) {
            return "all"; // New entity - all fields are new
        }
        if (after == null) {
            return "all"; // Deleted entity - all fields removed
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> beforeMap = objectMapper.convertValue(before, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> afterMap = objectMapper.convertValue(after, Map.class);

            List<String> changedFields = new ArrayList<>();
            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(beforeMap.keySet());
            allKeys.addAll(afterMap.keySet());

            for (String key : allKeys) {
                Object beforeValue = beforeMap.get(key);
                Object afterValue = afterMap.get(key);

                if (!Objects.equals(beforeValue, afterValue)) {
                    changedFields.add(key);
                }
            }

            return String.join(",", changedFields);
        } catch (Exception e) {
            log.warn("Failed to compute changed fields", e);
            return "";
        }
    }
}
