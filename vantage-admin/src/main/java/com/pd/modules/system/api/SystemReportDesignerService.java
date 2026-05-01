package com.pd.modules.system.api;

import com.pd.modules.system.report.domain.SysReportTemplate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * System module public API for report designer operations.
 */
public interface SystemReportDesignerService {

    List<SysReportTemplate> findAll();

    List<SysReportTemplate> findAllVersions();

    Optional<SysReportTemplate> findById(Long templateId);

    Optional<SysReportTemplate> findByTemplateKey(String templateKey);

    List<SysReportTemplate> findByTemplateKeyOrderByVersionDesc(String templateKey);

    SysReportTemplate save(SysReportTemplate template);

    boolean deleteById(Long templateId);

    List<Map<String, Object>> getDatasourceTables(String datasourceKey);

    List<Map<String, Object>> executeTemplate(Long templateId, String paramsJson);

    void exportReport(Long templateId, String paramsJson, String format, Object response) throws Exception;

    String buildSqlFromTemplate(SysReportTemplate template);

    List<Map<String, Object>> executeQuery(String datasourceKey, String sql);
}
