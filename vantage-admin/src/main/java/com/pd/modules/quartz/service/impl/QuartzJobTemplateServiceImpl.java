package com.pd.modules.quartz.service.impl;

import com.pd.modules.quartz.api.QuartzJobTemplateService;
import com.pd.modules.quartz.api.dto.JobDTO;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.service.JobTemplateService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuartzJobTemplateServiceImpl implements QuartzJobTemplateService {

    private final JobTemplateService jobTemplateService;

    public QuartzJobTemplateServiceImpl(JobTemplateService jobTemplateService) {
        this.jobTemplateService = jobTemplateService;
    }

    @Override
    public List<Map<String, Object>> getTemplates() {
        List<JobTemplateService.JobTemplate> templates = jobTemplateService.getTemplates();
        List<Map<String, Object>> result = new ArrayList<>();
        for (JobTemplateService.JobTemplate t : templates) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", t.name());
            map.put("jobName", t.jobName());
            map.put("jobGroup", t.jobGroup());
            map.put("invokeTarget", t.invokeTarget());
            map.put("cronExpression", t.cronExpression());
            map.put("description", t.description());
            map.put("maxRetryCount", t.maxRetryCount());
            map.put("retryInterval", t.retryInterval());
            map.put("timeoutSeconds", t.timeoutSeconds());
            result.add(map);
        }
        return result;
    }

    @Override
    public Map<String, Object> getTemplateByName(String name) {
        Optional<JobTemplateService.JobTemplate> opt = jobTemplateService.getTemplateByName(name);
        if (opt.isEmpty()) return null;
        JobTemplateService.JobTemplate t = opt.get();
        Map<String, Object> map = new HashMap<>();
        map.put("name", t.name());
        map.put("jobName", t.jobName());
        map.put("jobGroup", t.jobGroup());
        map.put("invokeTarget", t.invokeTarget());
        map.put("cronExpression", t.cronExpression());
        map.put("description", t.description());
        map.put("maxRetryCount", t.maxRetryCount());
        map.put("retryInterval", t.retryInterval());
        map.put("timeoutSeconds", t.timeoutSeconds());
        return map;
    }

    @Override
    public JobDTO createJobFromTemplate(String templateName, String customJobName) {
        SysJob entity = jobTemplateService.createJobFromTemplate(templateName, customJobName);
        return toDTO(entity);
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
