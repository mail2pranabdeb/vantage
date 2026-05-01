package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemConfigService;
import com.pd.modules.system.api.dto.ConfigDTO;
import com.pd.modules.system.domain.SysConfig;
import com.pd.modules.system.infrastructure.repository.SysConfigRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SysConfigRepository configRepository;

    public SystemConfigServiceImpl(SysConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public List<ConfigDTO> findAll() {
        return configRepository.findAll().stream()
            .map(this::toDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Optional<ConfigDTO> findById(Long configId) {
        return configRepository.findById(configId).map(this::toDTO);
    }

    @Override
    public Optional<ConfigDTO> findByConfigKey(String configKey) {
        return configRepository.findByConfigKey(configKey).map(this::toDTO);
    }

    @Override
    public String getConfigValue(String configKey) {
        return configRepository.findByConfigKey(configKey)
            .map(SysConfig::getConfigValue)
            .orElse("");
    }

    @Override
    @Transactional
    public ConfigDTO createConfig(ConfigDTO configDTO) {
        SysConfig config = toEntity(configDTO);
        return toDTO(configRepository.save(config));
    }

    @Override
    @Transactional
    public ConfigDTO updateConfig(ConfigDTO configDTO) {
        SysConfig config = toEntity(configDTO);
        return toDTO(configRepository.save(config));
    }

    @Override
    @Transactional
    public boolean deleteConfigByIds(Long[] configIds) {
        for (Long id : configIds) {
            configRepository.deleteById(id);
        }
        return true;
    }

    @Override
    public boolean existsByConfigKey(String configKey) {
        return configRepository.findByConfigKey(configKey).isPresent();
    }

    @Override
    @Transactional
    public void refreshCache() {
        configRepository.findAll();
    }

    private ConfigDTO toDTO(SysConfig entity) {
        ConfigDTO dto = new ConfigDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private SysConfig toEntity(ConfigDTO dto) {
        SysConfig entity = new SysConfig();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
