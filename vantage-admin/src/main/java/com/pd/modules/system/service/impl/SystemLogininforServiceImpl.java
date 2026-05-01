package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemLogininforService;
import com.pd.modules.system.api.dto.LogininforDTO;
import com.pd.modules.system.domain.SysLogininfor;
import com.pd.modules.system.infrastructure.repository.SysLogininforRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SystemLogininforServiceImpl implements SystemLogininforService {

    private final SysLogininforRepository logininforRepository;

    public SystemLogininforServiceImpl(SysLogininforRepository logininforRepository) {
        this.logininforRepository = logininforRepository;
    }

    @Override
    public List<LogininforDTO> findAll() {
        return logininforRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<LogininforDTO> findRecent(int limit) {
        return logininforRepository.findTopLogininforsByOrderByLoginTimeDesc(PageRequest.of(0, limit)).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<LogininforDTO> findByLoginName(String loginName) {
        return logininforRepository.findByLoginName(loginName).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<LogininforDTO> findByCondition(String loginName, String status, String ipaddr) {
        return logininforRepository.findByCondition(loginName, status, ipaddr).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<LogininforDTO> findFailedAttempts(int limit) {
        return logininforRepository.findByStatusOrderByLoginTimeDesc("1", PageRequest.of(0, limit)).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<LogininforDTO> findById(Long infoId) {
        return logininforRepository.findById(infoId).map(this::toDTO);
    }

    @Override
    @Transactional
    public boolean deleteByIds(Long[] infoIds) {
        for (Long id : infoIds) {
            logininforRepository.deleteById(id);
        }
        return true;
    }

    @Override
    @Transactional
    public void cleanLogs() {
        logininforRepository.deleteAll();
    }

    @Override
    @Transactional
    public void recordLogininfor(LogininforDTO logininforDTO) {
        SysLogininfor logininfor = toEntity(logininforDTO);
        logininforRepository.save(logininfor);
    }

    @Override
    public long count() {
        return logininforRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        return logininforRepository.countByStatus(status);
    }

    private LogininforDTO toDTO(SysLogininfor entity) {
        LogininforDTO dto = new LogininforDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private SysLogininfor toEntity(LogininforDTO dto) {
        SysLogininfor entity = new SysLogininfor();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
