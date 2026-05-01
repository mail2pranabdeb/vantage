package com.pd.modules.quartz.service.impl;

import com.pd.modules.quartz.api.QuartzEmailJobTemplateService;
import com.pd.modules.quartz.api.dto.EmailTemplateDTO;
import com.pd.modules.quartz.domain.EmailTemplate;
import com.pd.modules.quartz.service.EmailTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuartzEmailJobTemplateServiceImpl implements QuartzEmailJobTemplateService {

    private final EmailTemplateService emailTemplateService;

    public QuartzEmailJobTemplateServiceImpl(EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
    }

    @Override
    public List<EmailTemplateDTO> getAllTemplates() {
        return emailTemplateService.getAllTemplates().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmailTemplateDTO> getActiveTemplates() {
        return emailTemplateService.getActiveTemplates().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EmailTemplateDTO> getTemplateById(Long templateId) {
        return emailTemplateService.getTemplateById(templateId).map(this::toDTO);
    }

    @Override
    public Optional<EmailTemplateDTO> getTemplateByType(String templateType) {
        return emailTemplateService.getTemplateByType(templateType).map(this::toDTO);
    }

    @Override
    public EmailTemplateDTO saveTemplate(EmailTemplateDTO template) {
        EmailTemplate entity = toEntity(template);
        EmailTemplate saved = emailTemplateService.saveTemplate(entity);
        return toDTO(saved);
    }

    @Override
    public void deleteTemplate(Long templateId) {
        emailTemplateService.deleteTemplate(templateId);
    }

    @Override
    public void setTemplateAsDefault(Long templateId, String templateType) {
        emailTemplateService.setTemplateAsDefault(templateId, templateType);
    }

    @Override
    public String processTemplate(String template, Object job, Object jobLog) {
        return emailTemplateService.processTemplate(template, (com.pd.modules.quartz.domain.SysJob) job, (com.pd.modules.quartz.domain.SysJobLog) jobLog);
    }

    @Override
    public String executeMultipleQueriesAndRenderTables(String dataTablesJson, String paramsJson) {
        return emailTemplateService.executeMultipleQueriesAndRenderTables(dataTablesJson, paramsJson);
    }

    @Override
    public String executeQueryAndRenderTable(String datasourceKey, String querySql) {
        return emailTemplateService.executeQueryAndRenderTable(datasourceKey, querySql);
    }

    private EmailTemplateDTO toDTO(EmailTemplate entity) {
        if (entity == null) return null;
        EmailTemplateDTO dto = new EmailTemplateDTO();
        dto.setTemplateId(entity.getTemplateId());
        dto.setTemplateName(entity.getTemplateName());
        dto.setTemplateType(entity.getTemplateType());
        dto.setEmailSubject(entity.getEmailSubject());
        dto.setEmailBody(entity.getEmailBody());
        dto.setIsDefault(entity.getIsDefault());
        dto.setIsActive(entity.getIsActive());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.setRemark(entity.getRemark());
        dto.setDatasourceKey(entity.getDatasourceKey());
        dto.setQuerySql(entity.getQuerySql());
        dto.setIncludeDataTable(entity.getIncludeDataTable());
        dto.setDataTables(entity.getDataTables());
        dto.setPreviewParams(entity.getPreviewParams());
        dto.setRuntimeParams(entity.getRuntimeParams());
        return dto;
    }

    private EmailTemplate toEntity(EmailTemplateDTO dto) {
        if (dto == null) return null;
        EmailTemplate entity = new EmailTemplate();
        entity.setTemplateId(dto.getTemplateId());
        entity.setTemplateName(dto.getTemplateName());
        entity.setTemplateType(dto.getTemplateType());
        entity.setEmailSubject(dto.getEmailSubject());
        entity.setEmailBody(dto.getEmailBody());
        entity.setIsDefault(dto.getIsDefault());
        entity.setIsActive(dto.getIsActive());
        entity.setCreateTime(dto.getCreateTime());
        entity.setUpdateTime(dto.getUpdateTime());
        entity.setRemark(dto.getRemark());
        entity.setDatasourceKey(dto.getDatasourceKey());
        entity.setQuerySql(dto.getQuerySql());
        entity.setIncludeDataTable(dto.getIncludeDataTable());
        entity.setDataTables(dto.getDataTables());
        entity.setPreviewParams(dto.getPreviewParams());
        entity.setRuntimeParams(dto.getRuntimeParams());
        return entity;
    }
}
