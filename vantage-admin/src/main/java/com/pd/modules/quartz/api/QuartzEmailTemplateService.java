package com.pd.modules.quartz.api;

import com.pd.modules.quartz.api.dto.EmailTemplateDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QuartzEmailTemplateService {

    List<EmailTemplateDTO> findAll();

    List<EmailTemplateDTO> findActiveTemplates();

    Optional<EmailTemplateDTO> findById(Long templateId);

    Optional<EmailTemplateDTO> findByTemplateCode(String templateCode);

    EmailTemplateDTO createTemplate(EmailTemplateDTO template);

    EmailTemplateDTO updateTemplate(EmailTemplateDTO template);

    boolean deleteTemplate(Long templateId);

    boolean deleteTemplateByIds(Long[] templateIds);

    String renderTemplate(String templateCode, Map<String, Object> variables);

    void sendEmailFromTemplate(Long templateId, String toEmail, Map<String, Object> data);
}
