package com.pd.modules.system.report.api;

import com.pd.modules.system.report.domain.SysReportTemplate;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Report module public API.
 */
public interface ReportDesignerService {

    List<SysReportTemplate> findAll();

    List<SysReportTemplate> findAllVersions();

    List<Map<String, Object>> getJobsUsingTemplate(Long templateId);

    Optional<SysReportTemplate> findById(Long templateId);

    Optional<SysReportTemplate> findByTemplateKey(String templateKey);

    List<SysReportTemplate> findByTemplateKeyOrderByVersionDesc(String templateKey);

    SysReportTemplate save(SysReportTemplate template);

    boolean deleteById(Long templateId);

    List<Map<String, Object>> getDatasourceTables(String datasourceKey);

    String buildSqlFromTemplate(SysReportTemplate template);

    List<Map<String, Object>> executeTemplate(Long templateId, String paramsJson);

    List<Map<String, Object>> executeQuery(String datasourceKey, String sql);

    void exportReport(Long templateId, String paramsJson, String format, HttpServletResponse response) throws IOException;

    byte[] generateReportAttachment(Long templateId, String paramsJson, String format) throws IOException;

    Map<String, Object> getTemplateInfoForEmail(Long templateId);
}
