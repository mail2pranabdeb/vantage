package com.pd.modules.system.cache;

import com.pd.modules.system.domain.SysDictData;
import com.pd.modules.system.domain.SysDictType;
import com.pd.modules.system.infrastructure.repository.SysDictDataRepository;
import com.pd.modules.system.infrastructure.repository.SysDictTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dictionary Cache Service
 * Caches dictionary data for fast access and dynamic label lookup
 */
@Service
public class DictCacheService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DictCacheService.class);

    /**
     * Cache structure:
     * dictType -> List<SysDictData>
     */
    private final Map<String, List<SysDictData>> dictCache = new ConcurrentHashMap<>();

    /**
     * Reverse cache for quick label lookup:
     * dictType -> (dictValue -> dictLabel)
     */
    private final Map<String, Map<String, String>> labelCache = new ConcurrentHashMap<>();

    private final SysDictTypeRepository dictTypeRepository;
    private final SysDictDataRepository dictDataRepository;

    @Autowired
    public DictCacheService(
            SysDictTypeRepository dictTypeRepository,
            SysDictDataRepository dictDataRepository
    ) {
        this.dictTypeRepository = dictTypeRepository;
        this.dictDataRepository = dictDataRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing dictionary cache...");
        refreshCache();
    }

    /**
     * Refresh entire dictionary cache
     */
    public void refreshCache() {
        try {
            List<SysDictType> dictTypes = dictTypeRepository.findByStatus("0");
            
            for (SysDictType dictType : dictTypes) {
                String dictTypeStr = dictType.getDictType();
                List<SysDictData> dictDataList = dictDataRepository.findByDictTypeOrderBySort(dictTypeStr);
                
                dictCache.put(dictTypeStr, dictDataList);
                
                // Build label cache
                Map<String, String> labelMap = new HashMap<>();
                for (SysDictData data : dictDataList) {
                    labelMap.put(data.getDictValue(), data.getDictLabel());
                }
                labelCache.put(dictTypeStr, labelMap);
            }
            
            log.info("Dictionary cache refreshed: {} types loaded", dictCache.size());
        } catch (Exception e) {
            log.error("Failed to refresh dictionary cache", e);
        }
    }

    /**
     * Get dictionary label by value
     * @param dictType Dictionary type (e.g., "sys_normal_disable")
     * @param dictValue Dictionary value (e.g., "0")
     * @return Dictionary label (e.g., "Normal") or null if not found
     */
    public String getLabel(String dictType, String dictValue) {
        if (StringUtils.isEmpty(dictType) || StringUtils.isEmpty(dictValue)) {
            return null;
        }
        
        Map<String, String> labelMap = labelCache.get(dictType);
        if (labelMap != null) {
            return labelMap.get(dictValue);
        }
        
        // Cache miss - try to load from DB
        return loadDictLabel(dictType, dictValue);
    }

    /**
     * Get dictionary data list by type
     * @param dictType Dictionary type
     * @return List of dictionary data
     */
    public List<SysDictData> getDictDataByType(String dictType) {
        List<SysDictData> dictDataList = dictCache.get(dictType);
        if (dictDataList == null) {
            // Cache miss - load from DB
            dictDataList = dictDataRepository.findByDictTypeOrderBySort(dictType);
            if (dictDataList != null && !dictDataList.isEmpty()) {
                dictCache.put(dictType, dictDataList);
                
                // Update label cache
                Map<String, String> labelMap = new HashMap<>();
                for (SysDictData data : dictDataList) {
                    labelMap.put(data.getDictValue(), data.getDictLabel());
                }
                labelCache.put(dictType, labelMap);
            }
        }
        return dictDataList != null ? dictDataList : Collections.emptyList();
    }

    /**
     * Load dictionary label from database (cache miss fallback)
     */
    private String loadDictLabel(String dictType, String dictValue) {
        try {
            Optional<SysDictData> dictDataOpt = dictDataRepository.findByDictTypeAndValue(dictType, dictValue);
            if (dictDataOpt.isPresent()) {
                SysDictData dictData = dictDataOpt.get();
                
                // Update caches
                List<SysDictData> dictDataList = dictCache.computeIfAbsent(dictType, k -> new ArrayList<>());
                dictDataList.add(dictData);
                
                Map<String, String> labelMap = labelCache.computeIfAbsent(dictType, k -> new HashMap<>());
                labelMap.put(dictValue, dictData.getDictLabel());
                
                return dictData.getDictLabel();
            }
        } catch (Exception e) {
            log.error("Failed to load dictionary label from DB", e);
        }
        return null;
    }

    /**
     * Clear cache for specific dict type
     */
    public void clearCache(String dictType) {
        dictCache.remove(dictType);
        labelCache.remove(dictType);
        log.info("Dictionary cache cleared for type: {}", dictType);
    }

    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        return new CacheStats(
                dictCache.size(),
                labelCache.size(),
                dictCache.values().stream().mapToInt(List::size).sum()
        );
    }

    public record CacheStats(int dictTypes, int labelMaps, int totalEntries) {}
}
