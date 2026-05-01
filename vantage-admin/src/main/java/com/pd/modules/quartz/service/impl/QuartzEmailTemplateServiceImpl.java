package com.pd.modules.quartz.service.impl;

import com.pd.modules.quartz.api.QuartzEmailTemplateService;
import com.pd.modules.quartz.api.dto.EmailTemplateDTO;
import com.pd.modules.quartz.domain.EmailTemplate;
import com.pd.modules.quartz.infrastructure.repository.EmailTemplateRepository;
import com.pd.modules.quartz.service.EmailTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuartzEmailTemplateServiceImpl implements QuartzEmailTemplateService {

    private final EmailTemplateRepository templateRepository;
    private final EmailTemplateService emailTemplateService;

    public QuartzEmailTemplateServiceImpl(EmailTemplateRepository templateRepository,
                                          EmailTemplateService emailTemplateService) {
        this.templateRepository = templateRepository;
        this.emailTemplateService = emailTemplateService;
    }

    @Override
    public List<EmailTemplateDTO> findAll() {
        return templateRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmailTemplateDTO> findActiveTemplates() {
        return templateRepository.findByStatus("0").stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EmailTemplateDTO> findById(Long templateId) {
        return templateRepository.findById(templateId).map(this::toDTO);
    }

    @Override
    public Optional<EmailTemplateDTO> findByTemplateCode(String templateCode) {
        return templateRepository.findByTemplateCode(templateCode).map(this::toDTO);
    }

    @Override
    @Transactional
    public EmailTemplateDTO createTemplate(EmailTemplateDTO template) {
        EmailTemplate entity = toEntity(template);
        EmailTemplate saved = templateRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public EmailTemplateDTO updateTemplate(EmailTemplateDTO template) {
        EmailTemplate entity = toEntity(template);
        EmailTemplate saved = templateRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public boolean deleteTemplate(Long templateId) {
        if (templateRepository.existsById(templateId)) {
            templateRepository.deleteById(templateId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteTemplateByIds(Long[] templateIds) {
        for (Long id : templateIds) {
            deleteTemplate(id);
        }
        return true;
    }

    @Override
    public String renderTemplate(String templateCode, Map<String, Object> variables) {
        return emailTemplateService.renderTemplate(templateCode, variables);
    }

    @Override
    @Transactional
    public void sendEmailFromTemplate(Long templateId, String toEmail, Map<String, Object> data) {
        emailTemplateService.sendEmailFromTemplate(templateId, toEmail, data);
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
