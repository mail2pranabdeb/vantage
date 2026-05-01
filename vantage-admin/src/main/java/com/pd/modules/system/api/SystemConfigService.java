package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.ConfigDTO;
import java.util.List;
import java.util.Optional;

/**
 * System module public API for configuration operations.
 */
public interface SystemConfigService {

    List<ConfigDTO> findAll();

    Optional<ConfigDTO> findById(Long configId);

    Optional<ConfigDTO> findByConfigKey(String configKey);

    String getConfigValue(String configKey);

    ConfigDTO createConfig(ConfigDTO config);

    ConfigDTO updateConfig(ConfigDTO config);

    boolean deleteConfigByIds(Long[] configIds);

    boolean existsByConfigKey(String configKey);

    void refreshCache();
}
