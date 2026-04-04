package com.pd.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
            Map<String, Object> beforeMap = objectMapper.convertValue(before, Map.class);
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
