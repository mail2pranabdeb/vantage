package com.pd.modules.report.web;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.report.domain.SysReportTemplate;
import com.pd.modules.report.service.ReportDesignerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller for report designer.
 * Provides endpoints for visual report template management and execution.
 */
@RestController
@RequestMapping("/api/system/report-designer")
public class ReportDesignerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ReportDesignerController.class);

    private final ReportDesignerService reportDesignerService;

    public ReportDesignerController(ReportDesignerService reportDesignerService) {
        this.reportDesignerService = reportDesignerService;
    }

    // ==================== Template CRUD ====================

    @PreAuthorize("hasAuthority('system:report:template:query')")
    @GetMapping("/templates")
    public AjaxResult listTemplates() {
        List<SysReportTemplate> templates = reportDesignerService.findAll();
        return success(templates);
    }

    @PreAuthorize("hasAuthority('system:report:template:query')")
    @GetMapping("/templates/{templateId}")
    public AjaxResult getTemplate(@PathVariable Long templateId) {
        return reportDesignerService.findById(templateId)
            .map(this::success)
            .orElseGet(() -> error("Template not found"));
    }

    @PreAuthorize("hasAuthority('system:report:template:query')")
    @GetMapping("/templates/key/{templateKey}")
    public AjaxResult getTemplateByKey(@PathVariable String templateKey) {
        return reportDesignerService.findByTemplateKey(templateKey)
            .map(this::success)
            .orElseGet(() -> error("Template not found"));
    }

    @PreAuthorize("hasAuthority('system:report:template:add')")
    @Log(title = "Report Template", businessType = BusinessType.INSERT)
    @PostMapping("/templates")
    public AjaxResult addTemplate(@RequestBody SysReportTemplate template) {
        if (reportDesignerService.findByTemplateKey(template.getTemplateKey()).isPresent()) {
            return error("Template key already exists");
        }
        template.setCreateBy("admin");
        return success(reportDesignerService.save(template));
    }

    @PreAuthorize("hasAuthority('system:report:template:edit')")
    @Log(title = "Report Template", businessType = BusinessType.UPDATE)
    @PutMapping("/templates")
    public AjaxResult editTemplate(@RequestBody SysReportTemplate template) {
        if (reportDesignerService.findById(template.getTemplateId()).isEmpty()) {
            return error("Template not found");
        }
        template.setUpdateBy("admin");
        return success(reportDesignerService.save(template));
    }

    @PreAuthorize("hasAuthority('system:report:template:remove')")
    @Log(title = "Report Template", businessType = BusinessType.DELETE)
    @DeleteMapping("/templates/{templateId}")
    public AjaxResult deleteTemplate(@PathVariable Long templateId) {
        if (!reportDesignerService.deleteById(templateId)) {
            return error("Template not found");
        }
        return success("Template deleted successfully");
    }

    // ==================== Datasource Metadata ====================

    @PreAuthorize("hasAuthority('system:report:template:query')")
    @GetMapping("/datasource/{datasourceKey}/tables")
    public AjaxResult getDatasourceTables(@PathVariable String datasourceKey) {
        List<Map<String, Object>> tables = reportDesignerService.getDatasourceTables(datasourceKey);
        return success(tables);
    }

    // ==================== Report Execution ====================

    @PreAuthorize("hasAuthority('system:report:template:execute')")
    @Log(title = "Report Execution", businessType = BusinessType.OTHER)
    @PostMapping("/execute/{templateId}")
    public AjaxResult executeTemplate(@PathVariable Long templateId,
                                      @RequestParam(required = false, defaultValue = "{}") String params) {
        try {
            List<Map<String, Object>> data = reportDesignerService.executeTemplate(templateId, params);
            return success(data);
        } catch (Exception e) {
            logger.error("Failed to execute report template", e);
            return error("Failed to execute report: " + e.getMessage());
        }
    }

    // ==================== Export ====================

    @PreAuthorize("hasAuthority('system:report:template:execute')")
    @GetMapping("/export/{templateId}")
    public void exportTemplate(@PathVariable Long templateId,
                               @RequestParam(required = false, defaultValue = "{}") String params,
                               @RequestParam(required = false, defaultValue = "EXCEL") String format,
                               HttpServletResponse response) throws IOException {
        try {
            reportDesignerService.exportReport(templateId, params, format, response);
        } catch (Exception e) {
            logger.error("Failed to export report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Export failed: " + e.getMessage());
        }
    }

    // ==================== Preview ====================

    @PreAuthorize("hasAuthority('system:report:template:query')")
    @PostMapping("/preview")
    public AjaxResult previewReport(@RequestBody SysReportTemplate template) {
        try {
            // Build SQL from template (visual builder or manual SQL)
            String sql = reportDesignerService.buildSqlFromTemplate(template);
            logger.info("Preview SQL: {}", sql);

            // Replace parameters in SQL (for preview, use empty params)
            String paramsJson = "{}";

            // Execute the SQL directly (not from database)
            List<Map<String, Object>> data = reportDesignerService.executeQuery(
                template.getDatasourceKey(), sql);

            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("sql", sql);
            result.put("data", data);
            result.put("count", data.size());
            return success(result);
        } catch (Exception e) {
            logger.error("Failed to preview report", e);
            return error("Preview failed: " + e.getMessage());
        }
    }
}
