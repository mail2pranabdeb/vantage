package com.pd.modules.quartz.service.impl;

import com.pd.modules.quartz.api.QuartzJobGroupService;
import com.pd.modules.quartz.api.dto.JobDTO;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.service.JobGroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuartzJobGroupServiceImpl implements QuartzJobGroupService {

    private final JobGroupService jobGroupService;

    public QuartzJobGroupServiceImpl(JobGroupService jobGroupService) {
        this.jobGroupService = jobGroupService;
    }

    @Override
    public List<String> getAllJobGroups() {
        return jobGroupService.getAllJobGroups();
    }

    @Override
    public List<JobDTO> getJobsInGroup(String jobGroup) {
        return jobGroupService.getJobsInGroup(jobGroup).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> executeGroup(String jobGroup) {
        return jobGroupService.executeGroup(jobGroup);
    }

    @Override
    public List<Map<String, Object>> getJobGroupSummary() {
        return jobGroupService.getJobGroupSummary();
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
}
