package com.pd.modules.quartz.service.impl;

import com.pd.modules.quartz.api.QuartzJobLogService;
import com.pd.modules.quartz.api.dto.JobLogDTO;
import com.pd.modules.quartz.domain.SysJobLog;
import com.pd.modules.quartz.infrastructure.repository.SysJobLogRepository;
import com.pd.modules.quartz.service.ISysJobLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuartzJobLogServiceImpl implements QuartzJobLogService {

    private final ISysJobLogService sysJobLogService;
    private final SysJobLogRepository sysJobLogRepository;

    public QuartzJobLogServiceImpl(ISysJobLogService sysJobLogService, SysJobLogRepository sysJobLogRepository) {
        this.sysJobLogService = sysJobLogService;
        this.sysJobLogRepository = sysJobLogRepository;
    }

    @Override
    public List<JobLogDTO> findAll() {
        return sysJobLogService.selectJobLogList(new SysJobLog()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobLogDTO> findRecent(int limit) {
        SysJobLog query = new SysJobLog();
        return sysJobLogService.selectJobLogList(query).stream()
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobLogDTO> findByJobName(String jobName) {
        SysJobLog query = new SysJobLog();
        query.setJobName(jobName);
        return sysJobLogService.selectJobLogList(query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobLogDTO> findByStatus(String status) {
        SysJobLog query = new SysJobLog();
        query.setStatus(status);
        return sysJobLogService.selectJobLogList(query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<JobLogDTO> findByConditionPaginated(String jobName, String jobGroup, String status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return sysJobLogRepository.findByConditionPaginated(jobName, jobGroup, status, startTime, endTime, pageable).map(this::toDTO);
    }

    @Override
    public Optional<JobLogDTO> findById(Long jobLogId) {
        SysJobLog log = sysJobLogService.selectJobLogById(jobLogId);
        return Optional.ofNullable(log).map(this::toDTO);
    }

    @Override
    public boolean deleteByIds(Long[] jobLogIds) {
        return sysJobLogService.deleteJobLogByIds(jobLogIds) > 0;
    }

    @Override
    public void cleanLogs() {
        sysJobLogService.cleanJobLog();
    }

    @Override
    public long count() {
        return sysJobLogService.selectJobLogList(new SysJobLog()).size();
    }

    @Override
    public long countByStatus(String status) {
        SysJobLog query = new SysJobLog();
        query.setStatus(status);
        return sysJobLogService.selectJobLogList(query).size();
    }

    @Override
    public Map<String, Object> getJobMetrics(String jobName) {
        SysJobLog query = new SysJobLog();
        query.setJobName(jobName);
        List<SysJobLog> logs = sysJobLogService.selectJobLogList(query);
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalExecutions", logs.size());
        metrics.put("successCount", logs.stream().filter(l -> "0".equals(l.getStatus())).count());
        metrics.put("failureCount", logs.stream().filter(l -> "1".equals(l.getStatus())).count());
        return metrics;
    }

    private JobLogDTO toDTO(SysJobLog entity) {
        if (entity == null) return null;
        JobLogDTO dto = new JobLogDTO();
        dto.setJobLogId(entity.getJobLogId());
        dto.setJobId(entity.getJobId());
        dto.setJobName(entity.getJobName());
        dto.setJobGroup(entity.getJobGroup());
        dto.setInvokeTarget(entity.getInvokeTarget());
        dto.setJobMessage(entity.getJobMessage());
        dto.setStatus(entity.getStatus());
        dto.setExceptionInfo(entity.getExceptionInfo());
        dto.setExecutionDuration(entity.getExecutionDuration());
        dto.setRetryCount(entity.getRetryCount());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

    private SysJobLog toEntity(JobLogDTO dto) {
        if (dto == null) return null;
        SysJobLog entity = new SysJobLog();
        entity.setJobLogId(dto.getJobLogId());
        entity.setJobId(dto.getJobId());
        entity.setJobName(dto.getJobName());
        entity.setJobGroup(dto.getJobGroup());
        entity.setInvokeTarget(dto.getInvokeTarget());
        entity.setJobMessage(dto.getJobMessage());
        entity.setStatus(dto.getStatus());
        entity.setExceptionInfo(dto.getExceptionInfo());
        entity.setExecutionDuration(dto.getExecutionDuration());
        entity.setRetryCount(dto.getRetryCount());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setCreateTime(dto.getCreateTime());
        return entity;
    }
}
