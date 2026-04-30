package com.pd.gateway.report;

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
    public AjaxResult listTemplates(@RequestParam(required = false, defaultValue = "false") Boolean allVersions) {
        if (allVersions) {
            List<SysReportTemplate> templates = reportDesignerService.findAllVersions();
            return success(templates);
        }
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

    // ==================== Version Management ====================

    /**
     * Get all versions of a template
     */
    @PreAuthorize("hasAuthority('system:report:template:query')")
    @GetMapping("/templates/{templateKey}/versions")
    public AjaxResult getTemplateVersions(@PathVariable String templateKey) {
        List<SysReportTemplate> versions = reportDesignerService.findByTemplateKeyOrderByVersionDesc(templateKey);
        return success(versions);
    }

    /**
     * Get active versions for job scheduling dropdown
     */
    @PreAuthorize("hasAuthority('system:report:template:query')")
    @GetMapping("/templates/active-versions")
    public AjaxResult getActiveVersions() {
        List<SysReportTemplate> all = reportDesignerService.findAll();
        // Group by templateKey and get latest active version
        java.util.Map<String, SysReportTemplate> latest = new java.util.LinkedHashMap<>();
        for (SysReportTemplate t : all) {
            if ("0".equals(t.getStatus())) {
                latest.put(t.getTemplateKey(), t);
            }
        }
        return success(new java.util.ArrayList<>(latest.values()));
    }

    /**
     * Get all active templates (simplified for dropdowns)
     */
    @PreAuthorize("hasAuthority('system:report:template:query')")
    @GetMapping("/active-templates")
    public AjaxResult getActiveTemplates() {
        List<SysReportTemplate> all = reportDesignerService.findAll();
        List<SysReportTemplate> active = all.stream()
            .filter(t -> "0".equals(t.getStatus()))
            .collect(java.util.stream.Collectors.toList());
        return success(active);
    }

    /**
     * Archive a template version
     */
    @PreAuthorize("hasAuthority('system:report:template:edit')")
    @PutMapping("/templates/{templateId}/archive")
    public AjaxResult archiveTemplate(@PathVariable Long templateId) {
        return reportDesignerService.findById(templateId)
            .map(t -> {
                t.setStatus("2"); // Archived
                reportDesignerService.save(t);
                return success("Template archived");
            })
            .orElseGet(() -> error("Template not found"));
    }

    /**
     * Activate a report: Creates a new version and sets it to Active (0).
     * This allows users to "publish" changes as a new version.
     */
    @PreAuthorize("hasAuthority('system:report:template:edit')")
    @PutMapping("/templates/{templateId}/activate")
    @org.springframework.transaction.annotation.Transactional
    public AjaxResult activateTemplate(@PathVariable Long templateId) {
        return reportDesignerService.findById(templateId)
            .map(current -> {
                int newVersion = (current.getVersion() != null ? current.getVersion() : 0) + 1;
                current.setVersion(newVersion);
                current.setStatus("0"); // Active
                reportDesignerService.save(current);
                return success("Activated successfully as version " + newVersion);
            })
            .orElseGet(() -> error("Template not found"));
    }

    /**
     * Datasource Metadata
     */
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
                                      @RequestBody(required = false) Map<String, Object> params) {
        try {
            // Convert params Map to JSON string
            String paramsJson = "{}";
            if (params != null && !params.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                paramsJson = mapper.writeValueAsString(params);
            }
            List<Map<String, Object>> data = reportDesignerService.executeTemplate(templateId, paramsJson);
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
    public AjaxResult previewReport(@RequestBody SysReportTemplate template,
                            @RequestParam(required = false) String params) {
        try {
            // Build SQL from template (visual builder or manual SQL)
            String sql = reportDesignerService.buildSqlFromTemplate(template);
            logger.info("Preview SQL before params: {}", sql);

            // Replace parameters in SQL
            if (params != null && !params.isEmpty()) {
                sql = replaceParamsInSql(sql, params);
                logger.info("Preview SQL after params: {}", sql);
            }

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

    /**
     * Replace parameter placeholders in SQL with actual values
     */
    private String replaceParamsInSql(String sql, String paramsJson) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode params = mapper.readTree(paramsJson);
            if (params.isObject()) {
                java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = params.fields();
                while (fields.hasNext()) {
                    java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> field = fields.next();
                    String placeholder = ":" + field.getKey();
                    String value = field.getValue().asText();
                    sql = sql.replace(placeholder, "'" + value.replace("'", "''") + "'");
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse params JSON: {}", e.getMessage());
        }
        return sql;
    }
}
