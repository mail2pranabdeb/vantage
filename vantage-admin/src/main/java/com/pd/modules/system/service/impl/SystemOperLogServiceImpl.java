package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemOperLogService;
import com.pd.modules.system.api.dto.OperLogDTO;
import com.pd.modules.system.domain.SysOperLog;
import com.pd.modules.system.infrastructure.repository.SysOperLogRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SystemOperLogServiceImpl implements SystemOperLogService {

    private final SysOperLogRepository operLogRepository;

    public SystemOperLogServiceImpl(SysOperLogRepository operLogRepository) {
        this.operLogRepository = operLogRepository;
    }

    @Override
    public List<OperLogDTO> findAll() {
        return operLogRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<OperLogDTO> findRecent(int limit) {
        return operLogRepository.findTopOperLogsByOrderByOperTimeDesc(PageRequest.of(0, limit)).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<OperLogDTO> findById(Long operId) {
        return operLogRepository.findById(operId).map(this::toDTO);
    }

    @Override
    public List<OperLogDTO> findByOperName(String operName) {
        return operLogRepository.findByOperName(operName).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<OperLogDTO> findByTitle(String title) {
        return operLogRepository.findByTitle(title).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<OperLogDTO> findByCondition(String title, String operName, Integer businessType, Integer status) {
        return operLogRepository.findByCondition(title, operName, businessType, status).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public Page<OperLogDTO> findByConditionPaginated(String title, String operName, Integer businessType, Integer status, Pageable pageable) {
        return operLogRepository.findByConditionPaginated(title, operName, businessType, status, pageable).map(this::toDTO);
    }

    @Override
    @Transactional
    public boolean deleteByIds(Long[] operIds) {
        for (Long id : operIds) {
            operLogRepository.deleteById(id);
        }
        return true;
    }

    @Override
    @Transactional
    public void cleanLogs() {
        operLogRepository.deleteAll();
    }

    @Override
    public long count() {
        return operLogRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        return operLogRepository.countByStatus(status);
    }

    private OperLogDTO toDTO(SysOperLog entity) {
        OperLogDTO dto = new OperLogDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
