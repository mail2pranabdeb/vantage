package com.pd.modules.system.api;

import com.pd.modules.system.report.domain.SysReportTemplate;

import java.util.List;
import java.util.Map;

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

    List<?> listReportDesignerTemplates(boolean allVersions);

    Object getReportDesignerTemplate(Long templateId);

    Object getReportDesignerTemplateByKey(String templateKey);

    String addReportDesignerTemplate(SysReportTemplate template);

    String updateReportDesignerTemplate(SysReportTemplate template);

    String deleteReportDesignerTemplate(Long templateId);

    List<?> getReportDesignerTemplateVersions(String templateKey);

    List<?> getReportDesignerActiveVersions();

    List<?> getReportDesignerActiveTemplates();

    String archiveReportDesignerTemplate(Long templateId);

    String activateReportDesignerTemplate(Long templateId);

    List<Map<String, Object>> getDatasourceTables(String datasourceKey);

    List<Map<String, Object>> executeReportDesignerTemplate(Long templateId, Map<String, Object> params);
}
