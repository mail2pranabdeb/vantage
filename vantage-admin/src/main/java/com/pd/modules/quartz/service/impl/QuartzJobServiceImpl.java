package com.pd.modules.quartz.service.impl;

import com.pd.modules.quartz.api.QuartzJobService;
import com.pd.modules.quartz.api.dto.JobDTO;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.service.ISysJobService;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuartzJobServiceImpl implements QuartzJobService {

    private final ISysJobService sysJobService;

    public QuartzJobServiceImpl(ISysJobService sysJobService) {
        this.sysJobService = sysJobService;
    }

    @Override
    public List<JobDTO> findAll() {
        return sysJobService.selectJobList(new SysJob()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<JobDTO> findById(Long jobId) {
        SysJob job = sysJobService.selectJobById(jobId);
        return Optional.ofNullable(job).map(this::toDTO);
    }

    @Override
    public JobDTO createJob(JobDTO job) {
        try {
            SysJob entity = toEntity(job);
            sysJobService.insertJob(entity);
            return toDTO(entity);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to create job", e);
        }
    }

    @Override
    public JobDTO updateJob(JobDTO job) {
        try {
            SysJob entity = toEntity(job);
            sysJobService.updateJob(entity);
            return toDTO(entity);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to update job", e);
        }
    }

    @Override
    public boolean deleteJob(Long jobId) {
        try {
            SysJob job = sysJobService.selectJobById(jobId);
            if (job != null) {
                sysJobService.deleteJob(job);
                return true;
            }
            return false;
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to delete job", e);
        }
    }

    @Override
    public void runJob(Long jobId) {
        try {
            SysJob job = sysJobService.selectJobById(jobId);
            if (job != null) {
                sysJobService.run(job);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to run job", e);
        }
    }

    @Override
    public void pauseJob(Long jobId) {
        try {
            SysJob job = sysJobService.selectJobById(jobId);
            if (job != null) {
                sysJobService.pauseJob(job);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to pause job", e);
        }
    }

    @Override
    public void resumeJob(Long jobId) {
        try {
            SysJob job = sysJobService.selectJobById(jobId);
            if (job != null) {
                sysJobService.resumeJob(job);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to resume job", e);
        }
    }

    private JobDTO toDTO(SysJob entity) {
        if (entity == null) return null;
        JobDTO dto = new JobDTO();
        dto.setJobId(entity.getJobId());
        dto.setJobName(entity.getJobName());
        dto.setJobGroup(entity.getJobGroup());
        dto.setJobType(entity.getJobType());
        dto.setReportId(entity.getReportId());
        dto.setReportParams(entity.getReportParams());
        dto.setEmailTemplateParams(entity.getEmailTemplateParams());
        dto.setReportEmailGroup(entity.getReportEmailGroup());
        dto.setInvokeTarget(entity.getInvokeTarget());
        dto.setCronExpression(entity.getCronExpression());
        dto.setMisfirePolicy(entity.getMisfirePolicy());
        dto.setConcurrent(entity.getConcurrent());
        dto.setStatus(entity.getStatus());
        dto.setCreateBy(entity.getCreateBy());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateBy(entity.getUpdateBy());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.setRemark(entity.getRemark());
        dto.setMaxRetryCount(entity.getMaxRetryCount());
        dto.setRetryInterval(entity.getRetryInterval());
        dto.setTimeoutSeconds(entity.getTimeoutSeconds());
        dto.setNotifyOnFailure(entity.getNotifyOnFailure());
        dto.setNotificationEmails(entity.getNotificationEmails());
        dto.setEmailTemplateId(entity.getEmailTemplateId());
        dto.setWebhookUrl(entity.getWebhookUrl());
        dto.setDependentJobIds(entity.getDependentJobIds());
        dto.setTimeZone(entity.getTimeZone());
        dto.setAllowHoliday(entity.getAllowHoliday());
        dto.setTemplateName(entity.getTemplateName());
        dto.setWebhookToken(entity.getWebhookToken());
        return dto;
    }

    private SysJob toEntity(JobDTO dto) {
        if (dto == null) return null;
        SysJob entity = new SysJob();
        entity.setJobId(dto.getJobId());
        entity.setJobName(dto.getJobName());
        entity.setJobGroup(dto.getJobGroup());
        entity.setJobType(dto.getJobType());
        entity.setReportId(dto.getReportId());
        entity.setReportParams(dto.getReportParams());
        entity.setEmailTemplateParams(dto.getEmailTemplateParams());
        entity.setReportEmailGroup(dto.getReportEmailGroup());
        entity.setInvokeTarget(dto.getInvokeTarget());
        entity.setCronExpression(dto.getCronExpression());
        entity.setMisfirePolicy(dto.getMisfirePolicy());
        entity.setConcurrent(dto.getConcurrent());
        entity.setStatus(dto.getStatus());
        entity.setCreateBy(dto.getCreateBy());
        entity.setCreateTime(dto.getCreateTime());
        entity.setUpdateBy(dto.getUpdateBy());
        entity.setUpdateTime(dto.getUpdateTime());
        entity.setRemark(dto.getRemark());
        entity.setMaxRetryCount(dto.getMaxRetryCount());
        entity.setRetryInterval(dto.getRetryInterval());
        entity.setTimeoutSeconds(dto.getTimeoutSeconds());
        entity.setNotifyOnFailure(dto.getNotifyOnFailure());
        entity.setNotificationEmails(dto.getNotificationEmails());
        entity.setEmailTemplateId(dto.getEmailTemplateId());
        entity.setWebhookUrl(dto.getWebhookUrl());
        entity.setDependentJobIds(dto.getDependentJobIds());
        entity.setTimeZone(dto.getTimeZone());
        entity.setAllowHoliday(dto.getAllowHoliday());
        entity.setTemplateName(dto.getTemplateName());
        entity.setWebhookToken(dto.getWebhookToken());
        return entity;
    }
}
