package com.pd.modules.system.service.impl;

import com.pd.modules.system.report.domain.SysReportTemplate;
import com.pd.modules.system.report.api.ReportDesignerService;
import com.pd.modules.system.api.SystemReportDesignerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SystemReportDesignerServiceImpl implements SystemReportDesignerService {

    private final ReportDesignerService reportDesignerService;

    public SystemReportDesignerServiceImpl(ReportDesignerService reportDesignerService) {
        this.reportDesignerService = reportDesignerService;
    }

    @Override
    public List<SysReportTemplate> findAll() {
        return reportDesignerService.findAll();
    }

    @Override
    public List<SysReportTemplate> findAllVersions() {
        return reportDesignerService.findAllVersions();
    }

    @Override
    public Optional<SysReportTemplate> findById(Long templateId) {
        return reportDesignerService.findById(templateId);
    }

    @Override
    public Optional<SysReportTemplate> findByTemplateKey(String templateKey) {
        return reportDesignerService.findByTemplateKey(templateKey);
    }

    @Override
    public List<SysReportTemplate> findByTemplateKeyOrderByVersionDesc(String templateKey) {
        return reportDesignerService.findByTemplateKeyOrderByVersionDesc(templateKey);
    }

    @Override
    public SysReportTemplate save(SysReportTemplate template) {
        return reportDesignerService.save(template);
    }

    @Override
    public boolean deleteById(Long templateId) {
        return reportDesignerService.deleteById(templateId);
    }

    @Override
    public List<Map<String, Object>> getDatasourceTables(String datasourceKey) {
        return reportDesignerService.getDatasourceTables(datasourceKey);
    }

    @Override
    public List<Map<String, Object>> executeTemplate(Long templateId, String paramsJson) {
        return reportDesignerService.executeTemplate(templateId, paramsJson);
    }

    @Override
    public void exportReport(Long templateId, String paramsJson, String format, Object response) throws Exception {
        reportDesignerService.exportReport(templateId, paramsJson, format, (jakarta.servlet.http.HttpServletResponse) response);
    }

    @Override
    public String buildSqlFromTemplate(SysReportTemplate template) {
        return reportDesignerService.buildSqlFromTemplate(template);
    }

    @Override
    public List<Map<String, Object>> executeQuery(String datasourceKey, String sql) {
        return reportDesignerService.executeQuery(datasourceKey, sql);
    }
}
