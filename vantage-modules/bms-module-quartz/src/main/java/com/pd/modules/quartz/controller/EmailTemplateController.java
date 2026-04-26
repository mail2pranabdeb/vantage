package com.pd.modules.quartz.controller;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.quartz.domain.EmailTemplate;
import com.pd.modules.quartz.service.EmailTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Email template controller for managing job notification templates
 */
@RestController
@RequestMapping("/api/system/email-template")
public class EmailTemplateController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateController.class);

    @Autowired
    private EmailTemplateService emailTemplateService;

    /**
     * Get all templates
     */
    @GetMapping("/list")
    public AjaxResult list() {
        List<EmailTemplate> templates = emailTemplateService.getAllTemplates();
        return success(templates);
    }

    /**
     * Get active templates
     */
    @GetMapping("/active")
    public AjaxResult getActive() {
        List<EmailTemplate> templates = emailTemplateService.getActiveTemplates();
        return success(templates);
    }

    /**
     * Get template by ID
     */
    @GetMapping("/{templateId}")
    public AjaxResult getById(@PathVariable Long templateId) {
        return emailTemplateService.getTemplateById(templateId)
                .map(this::success)
                .orElse(error("Template not found"));
    }

    /**
     * Get template by type
     */
    @GetMapping("/type/{templateType}")
    public AjaxResult getByType(@PathVariable String templateType) {
        return emailTemplateService.getTemplateByType(templateType)
                .map(this::success)
                .orElse(error("Template not found for type: " + templateType));
    }

    /**
     * Create new template
     */
    @PostMapping
    public AjaxResult add(@RequestBody EmailTemplate template) {
        emailTemplateService.saveTemplate(template);
        return success("Template created successfully");
    }

    /**
     * Update template
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EmailTemplate template) {
        emailTemplateService.saveTemplate(template);
        return success("Template updated successfully");
    }

    /**
     * Delete template
     */
    @DeleteMapping("/{templateId}")
    public AjaxResult remove(@PathVariable Long templateId) {
        emailTemplateService.deleteTemplate(templateId);
        return success("Template deleted successfully");
    }

    /**
     * Set template as default
     */
    @PutMapping("/{templateId}/set-default")
    public AjaxResult setAsDefault(
            @PathVariable Long templateId,
            @RequestParam String templateType) {
        emailTemplateService.setTemplateAsDefault(templateId, templateType);
        return success("Template set as default");
    }

    /**
     * Toggle template active status
     */
    @PutMapping("/{templateId}/toggle-active")
    public AjaxResult toggleActive(@PathVariable Long templateId) {
        EmailTemplate template = emailTemplateService.getTemplateById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        template.setIsActive(!template.getIsActive());
        emailTemplateService.saveTemplate(template);
        return success("Template status updated");
    }

    /**
     * Preview template with sample data and optional SQL execution
     */
    @PostMapping("/preview")
    public AjaxResult preview(@RequestBody Map<String, Object> request) {
        try {
            String subject = (String) request.getOrDefault("emailSubject", "");
            String body = (String) request.getOrDefault("emailBody", "");
            String dataTablesJson = (String) request.get("dataTables");
            String params = (String) request.get("params");

            log.info("Preview request - body contains dataTable placeholder: {}", body.contains("${dataTable}"));
            log.info("Preview request - dataTablesJson: {}", dataTablesJson);
            log.info("Preview request - params: {}", params);

            // Process template variables
            String renderedSubject = emailTemplateService.processTemplate(subject, null, null);
            String renderedBody = emailTemplateService.processTemplate(body, null, null);
            log.info("Preview request - after processTemplate body has ${dataTable}: {}", renderedBody.contains("${dataTable}"));

            String dataTableHtml = "";
            if (dataTablesJson != null && !dataTablesJson.isEmpty()) {
                dataTableHtml = emailTemplateService.executeMultipleQueriesAndRenderTables(dataTablesJson, params);
                log.info("Preview request - dataTableHtml generated, length: {}, contains tables: {}", dataTableHtml.length(), dataTableHtml.contains("<table"));
                renderedBody = renderedBody.replace("${dataTable}", dataTableHtml);
                log.info("Preview request - after replace, body contains table: {}", renderedBody.contains("<table"));
            } else {
                renderedBody = renderedBody.replace("${dataTable}", "");
            }

            AjaxResult result = success();
            result.put("subject", renderedSubject);
            result.put("body", renderedBody);
            return result;
        } catch (Exception e) {
            log.error("Preview failed", e);
            return error("Preview failed: " + e.getMessage());
        }
    }
}
