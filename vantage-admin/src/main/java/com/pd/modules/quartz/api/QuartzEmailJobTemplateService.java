package com.pd.modules.quartz.api;

import com.pd.modules.quartz.api.dto.EmailTemplateDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QuartzEmailJobTemplateService {

    List<EmailTemplateDTO> getAllTemplates();

    List<EmailTemplateDTO> getActiveTemplates();

    Optional<EmailTemplateDTO> getTemplateById(Long templateId);

    Optional<EmailTemplateDTO> getTemplateByType(String templateType);

    EmailTemplateDTO saveTemplate(EmailTemplateDTO template);

    void deleteTemplate(Long templateId);

    void setTemplateAsDefault(Long templateId, String templateType);

    String processTemplate(String template, Object job, Object jobLog);

    String executeMultipleQueriesAndRenderTables(String dataTablesJson, String paramsJson);

    String executeQueryAndRenderTable(String datasourceKey, String querySql);
}
