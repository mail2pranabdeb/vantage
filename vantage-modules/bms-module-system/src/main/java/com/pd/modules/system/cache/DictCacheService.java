package com.pd.modules.system.cache;

import com.pd.modules.system.domain.SysDictData;
import com.pd.modules.system.domain.SysDictType;
import com.pd.modules.system.infrastructure.repository.SysDictDataRepository;
import com.pd.modules.system.infrastructure.repository.SysDictTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Dictionary Cache Service with Spring Cache annotations
 */
@Service
public class DictCacheService {

    private static final Logger log = LoggerFactory.getLogger(DictCacheService.class);

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

    /**
     * Get dictionary data by type with caching
     */
    @Cacheable(value = "dictData", key = "#dictType")
    public List<SysDictData> getDictDataByType(String dictType) {
        log.debug("Loading dict data from DB for type: {}", dictType);
        return dictDataRepository.findByDictTypeOrderBySort(dictType);
    }

    /**
     * Get dictionary label with caching
     */
    @Cacheable(value = "dictLabel", key = "#dictType + '_' + #dictValue")
    public String getDictLabel(String dictType, String dictValue) {
        log.debug("Loading dict label from DB for type: {}, value: {}", dictType, dictValue);
        Optional<SysDictData> dictDataOpt = dictDataRepository.findByDictTypeAndValue(dictType, dictValue);
        return dictDataOpt.map(SysDictData::getDictLabel).orElse(null);
    }

    /**
     * Clear cache for specific dict type
     */
    @CacheEvict(value = {"dictData", "dictLabel"}, key = "#dictType")
    public void clearCache(String dictType) {
        log.info("Cache cleared for dict type: {}", dictType);
    }

    /**
     * Clear all dict data cache
     */
    @CacheEvict(value = "dictData", allEntries = true)
    public void clearAllDictDataCache() {
        log.info("All dict data cache cleared");
    }

    /**
     * Clear all dict label cache
     */
    @CacheEvict(value = "dictLabel", allEntries = true)
    public void clearAllDictLabelCache() {
        log.info("All dict label cache cleared");
    }

    /**
     * Get dict cache size (placeholder - Spring Cache doesn't expose size directly)
     */
    public int getCacheSize() {
        return 0;
    }

    /**
     * Get label cache size
     */
    public int getLabelCacheSize() {
        return 0;
    }
}
