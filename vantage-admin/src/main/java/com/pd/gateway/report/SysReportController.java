package com.pd.gateway.report;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.report.domain.SysReport;
import com.pd.modules.report.domain.SysReportTemplate;
import com.pd.modules.report.service.ReportDesignerService;
import com.pd.modules.report.service.SysReportService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/system/report")
public class SysReportController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(SysReportController.class);

    @Autowired
    private SysReportService reportService;

    @Autowired
    private ReportDesignerService reportDesignerService;

    @Autowired
    private Scheduler scheduler;

    @PreAuthorize("hasAuthority('system:report:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        List<SysReport> reports = reportService.findAll();
        return success(reports);
    }

    @PreAuthorize("hasAuthority('system:report:query')")
    @GetMapping(value = "/{reportId}")
    public AjaxResult getInfo(@PathVariable Long reportId) {
        Optional<SysReport> report = reportService.findById(reportId);
        return report.map(this::success).orElseGet(() -> error("Report not found"));
    }

    @PreAuthorize("hasAuthority('system:report:add')")
    @Log(title = "Report Management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysReport report) {
        if (reportService.existsByReportKey(report.getReportKey())) {
            return error("Report key already exists");
        }
        reportService.save(report);
        return success("Report added successfully");
    }

    @PreAuthorize("hasAuthority('system:report:edit')")
    @Log(title = "Report Management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysReport report) {
        Optional<SysReport> existing = reportService.findById(report.getReportId());
        if (!existing.isPresent()) {
            return error("Report not found");
        }
        
        if (!existing.get().getReportKey().equals(report.getReportKey()) && 
            reportService.existsByReportKey(report.getReportKey())) {
            return error("Report key already exists");
        }
        
        reportService.save(report);
        return success("Report updated successfully");
    }

    @PreAuthorize("hasAuthority('system:report:remove')")
    @Log(title = "Report Management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{reportId}")
    public AjaxResult remove(@PathVariable Long reportId) {
        reportService.deleteById(reportId);
        return success("Report deleted successfully");
    }

    @PreAuthorize("hasAuthority('system:report:execute')")
    @Log(title = "Report Management", businessType = BusinessType.OTHER)
    @PostMapping("/execute/{reportId}")
    public AjaxResult execute(@PathVariable Long reportId, @RequestBody(required = false) Map<String, Object> params) {
        try {
            // Convert params Map to JSON string for parameter substitution
            String paramsJson = "{}";
            if (params != null && !params.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                paramsJson = mapper.writeValueAsString(params);
            }
            
            List<Map<String, Object>> results = reportService.executeReport(reportId, paramsJson);
            return success(results);
        } catch (Exception e) {
            log.error("Report execution failed", e);
            return error("Failed to execute report: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('system:report:execute')")
    @Log(title = "Report Management", businessType = BusinessType.EXPORT)
    @GetMapping("/download/{reportId}")
    public void download(@PathVariable Long reportId,
                         @RequestParam(required = false) String params,
                         @RequestParam(required = false, defaultValue = "EXCEL") String format,
                         HttpServletResponse response) throws Exception {
        List<Map<String, Object>> data = reportService.executeReport(reportId, params);
        SysReport report = reportService.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));

        reportService.downloadReport(response, report, data, format);
    }

    // ==================== Template-Based Report Creation ====================

    /**
     * Get available templates for report creation
     */
    @PreAuthorize("hasAuthority('system:report:list')")
    @GetMapping("/templates")
    public AjaxResult getTemplates() {
        return success(reportDesignerService.findAll());
    }

    /**
     * Get a specific template by ID
     */
    @PreAuthorize("hasAuthority('system:report:list')")
    @GetMapping("/template/{templateId}")
    public AjaxResult getTemplate(@PathVariable Long templateId) {
        return reportDesignerService.findById(templateId)
            .map(this::success)
            .orElseGet(() -> error("Template not found"));
    }

    /**
     * Create a report from a template
     * Populates the report with template's configuration
     */
    @PreAuthorize("hasAuthority('system:report:add')")
    @Log(title = "Report from Template", businessType = BusinessType.INSERT)
    @PostMapping("/from-template")
    public AjaxResult createFromTemplate(@RequestBody Map<String, Object> request) {
        try {
            Long templateId = Long.valueOf(request.get("templateId").toString());
            String reportName = (String) request.get("reportName");
            String reportKey = (String) request.get("reportKey");
            String scheduleCron = (String) request.get("scheduleCron");
            String emailRecipients = (String) request.get("emailRecipients");
            String emailSubject = (String) request.get("emailSubject");

            // Get template
            Optional<SysReportTemplate> templateOpt = reportDesignerService.findById(templateId);
            if (templateOpt.isEmpty()) {
                return error("Template not found");
            }

            SysReportTemplate template = templateOpt.get();

            // Create report from template
            SysReport report = new SysReport();
            report.setReportName(reportName != null ? reportName : template.getTemplateName() + " Report");
            report.setReportKey(reportKey != null ? reportKey : template.getTemplateKey() + "_report");
            report.setReportType("SQL");
            report.setDatasourceKey(template.getDatasourceKey());
            report.setSqlContent(template.getSqlContent());
            report.setColumnsConfig(template.getColumnsConfig());
            report.setParamsConfig(template.getFiltersConfig());
            report.setOutputFormat(template.getOutputFormat() != null ? template.getOutputFormat() : "EXCEL");
            report.setTemplateId(templateId);
            report.setCreateBy("admin");

            // Set scheduling if provided
            if (scheduleCron != null && !scheduleCron.isEmpty()) {
                report.setScheduleEnabled(true);
                report.setScheduleCron(scheduleCron);
            }

            // Set email if provided
            if (emailRecipients != null && !emailRecipients.isEmpty()) {
                report.setEmailEnabled(true);
                report.setEmailRecipients(emailRecipients);
                report.setEmailSubject(emailSubject != null ? emailSubject : report.getReportName());
            }

            if (reportService.existsByReportKey(report.getReportKey())) {
                return error("Report key already exists: " + report.getReportKey());
            }

            reportService.save(report);
            return success("Report created from template successfully", report);
        } catch (Exception e) {
            log.error("Failed to create report from template", e);
            return error("Failed to create report: " + e.getMessage());
        }
    }

    // ==================== Job Scheduling ====================

    /**
     * Schedule a report/template as a Quartz job with email delivery
     * Accepts both reportId and templateId (for backward compatibility)
     */
    @PreAuthorize("hasAuthority('system:report:edit')")
    @Log(title = "Schedule Report", businessType = BusinessType.OTHER)
    @PostMapping("/schedule/{templateId}")
    public AjaxResult scheduleReport(@PathVariable Long templateId, @RequestBody Map<String, Object> config) {
        try {
            // Try to get as template first
            var templateOpt = reportDesignerService.findById(templateId);
            String reportName = templateOpt.map(t -> t.getTemplateName()).orElse("Report_" + templateId);
            String defaultFormat = templateOpt.map(t -> t.getOutputFormat()).orElse("EXCEL");

            String cronExpression = (String) config.get("cronExpression");
            String recipients = (String) config.get("recipients");
            String subject = (String) config.get("subject");
            String body = (String) config.get("body");
            String format = (String) config.getOrDefault("format", defaultFormat);
            String params = (String) config.getOrDefault("params", "{}");

            if (cronExpression == null || cronExpression.isEmpty()) {
                return error("Cron expression is required");
            }
            if (recipients == null || recipients.isEmpty()) {
                return error("Email recipients are required");
            }

            // Create Quartz job
            String jobName = "report_" + templateId;
            String jobGroup = "reports";

            JobDetail jobDetail = JobBuilder.newJob(com.pd.modules.report.job.ReportScheduleJob.class)
                .withIdentity(jobName, jobGroup)
                .withDescription("Report: " + reportName)
                .build();

            jobDetail.getJobDataMap().put("templateId", templateId);
            jobDetail.getJobDataMap().put("params", params);
            jobDetail.getJobDataMap().put("recipients", recipients);
            jobDetail.getJobDataMap().put("ccEmails", config.get("ccEmails"));
            jobDetail.getJobDataMap().put("subject", subject != null ? subject : reportName + " Report");
            jobDetail.getJobDataMap().put("body", body != null ? body : "Please find the attached report.");
            jobDetail.getJobDataMap().put("format", format);

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "_trigger", jobGroup)
                .withSchedule(scheduleBuilder)
                .build();

            // Delete existing job if any
            scheduler.deleteJob(new JobKey(jobName, jobGroup));

            // Schedule the job
            scheduler.scheduleJob(jobDetail, trigger);

            Map<String, Object> result = new HashMap<>();
            result.put("jobName", jobName);
            result.put("jobGroup", jobGroup);
            result.put("cronExpression", cronExpression);
            result.put("nextFireTime", trigger.getNextFireTime());

            return success("Report scheduled successfully", result);
        } catch (Exception e) {
            log.error("Failed to schedule report", e);
            return error("Failed to schedule report: " + e.getMessage());
        }
    }

    /**
     * Unschedule a report (delete Quartz job)
     */
    @PreAuthorize("hasAuthority('system:report:edit')")
    @Log(title = "Unschedule Report", businessType = BusinessType.OTHER)
    @DeleteMapping("/unschedule/{reportId}")
    public AjaxResult unscheduleReport(@PathVariable Long reportId) {
        try {
            Optional<SysReport> reportOpt = reportService.findById(reportId);
            if (reportOpt.isEmpty()) {
                return error("Report not found");
            }

            SysReport report = reportOpt.get();
            String jobName = "report_" + reportId;
            String jobGroup = "reports";

            // Delete Quartz job
            scheduler.deleteJob(new JobKey(jobName, jobGroup));

            // Update report
            report.setScheduleEnabled(false);
            report.setScheduleCron(null);
            reportService.save(report);

            return success("Report unscheduled successfully");
        } catch (Exception e) {
            log.error("Failed to unschedule report", e);
            return error("Failed to unschedule report: " + e.getMessage());
        }
    }
}
