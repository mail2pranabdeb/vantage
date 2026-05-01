package com.pd.modules.system.api;

import java.util.List;
import java.util.Map;

/**
 * System module public API for report entity operations.
 */
public interface SystemReportEntityService {

    List<?> findAllReports();

    Object findById(Long reportId);

    String createReport(Object report);

    String updateReport(Object report);

    String deleteReport(Long reportId);

    List<Map<String, Object>> executeReport(Long reportId, String paramsJson);

    List<?> getTemplates();

    Object getTemplate(Long templateId);

    String createReportFromTemplate(Map<String, Object> request);

    String scheduleReport(Long templateId, Map<String, Object> config);

    String unscheduleReport(Long reportId);
}
