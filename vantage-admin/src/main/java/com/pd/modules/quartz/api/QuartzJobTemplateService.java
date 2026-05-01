package com.pd.modules.quartz.api;

import com.pd.modules.quartz.api.dto.JobDTO;
import java.util.List;
import java.util.Map;

/**
 * Quartz module public API for job template operations.
 */
public interface QuartzJobTemplateService {

    List<Map<String, Object>> getTemplates();

    Map<String, Object> getTemplateByName(String name);

    JobDTO createJobFromTemplate(String templateName, String customJobName);
}
