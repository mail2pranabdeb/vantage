package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemReportEntityService;
import com.pd.modules.system.report.api.ReportDesignerService;
import com.pd.modules.system.report.domain.SysReport;
import com.pd.modules.system.report.domain.SysReportTemplate;
import com.pd.modules.system.report.service.SysReportService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SystemReportEntityServiceImpl implements SystemReportEntityService {

    private static final Logger log = LoggerFactory.getLogger(SystemReportEntityServiceImpl.class);

    private final SysReportService systemReportService;
    private final ReportDesignerService reportDesignerService;
    private final Scheduler scheduler;

    public SystemReportEntityServiceImpl(SysReportService systemReportService, ReportDesignerService reportDesignerService, Scheduler scheduler) {
        this.systemReportService = systemReportService;
        this.reportDesignerService = reportDesignerService;
        this.scheduler = scheduler;
    }

    @Override
    public List<?> findAllReports() {
        return systemReportService.findAll();
    }

    @Override
    public Object findById(Long reportId) {
        return systemReportService.findById(reportId).orElse(null);
    }

    @Override
    public String createReport(Object report) {
        if (!(report instanceof SysReport sysReport)) {
            return "Invalid report object";
        }
        if (systemReportService.existsByReportKey(sysReport.getReportKey())) {
            return "Report key already exists";
        }
        systemReportService.save(sysReport);
        return "Report added successfully";
    }

    @Override
    public String updateReport(Object report) {
        if (!(report instanceof SysReport sysReport)) {
            return "Invalid report object";
        }
        var existing = systemReportService.findById(sysReport.getReportId());
        if (existing.isEmpty()) {
            return "Report not found";
        }
        if (!existing.get().getReportKey().equals(sysReport.getReportKey()) &&
                systemReportService.existsByReportKey(sysReport.getReportKey())) {
            return "Report key already exists";
        }
        systemReportService.save(sysReport);
        return "Report updated successfully";
    }

    @Override
    public String deleteReport(Long reportId) {
        systemReportService.deleteById(reportId);
        return "Report deleted successfully";
    }

    @Override
    public List<Map<String, Object>> executeReport(Long reportId, String paramsJson) {
        try {
            return systemReportService.executeReport(reportId, paramsJson);
        } catch (Exception e) {
            log.error("Report execution failed", e);
            throw new RuntimeException("Failed to execute report: " + e.getMessage());
        }
    }

    @Override
    public List<?> getTemplates() {
        return reportDesignerService.findAll();
    }

    @Override
    public Object getTemplate(Long templateId) {
        return reportDesignerService.findById(templateId).orElse(null);
    }

    @Override
    public String createReportFromTemplate(Map<String, Object> request) {
        try {
            Long templateId = Long.valueOf(request.get("templateId").toString());
            String reportName = (String) request.get("reportName");
            String reportKey = (String) request.get("reportKey");
            String scheduleCron = (String) request.get("scheduleCron");
            String emailRecipients = (String) request.get("emailRecipients");
            String emailSubject = (String) request.get("emailSubject");

            var templateOpt = reportDesignerService.findById(templateId);
            if (templateOpt.isEmpty()) {
                return "Template not found";
            }
            SysReportTemplate template = templateOpt.get();

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

            if (scheduleCron != null && !scheduleCron.isEmpty()) {
                report.setScheduleEnabled(true);
                report.setScheduleCron(scheduleCron);
            }
            if (emailRecipients != null && !emailRecipients.isEmpty()) {
                report.setEmailEnabled(true);
                report.setEmailRecipients(emailRecipients);
                report.setEmailSubject(emailSubject != null ? emailSubject : report.getReportName());
            }

            if (systemReportService.existsByReportKey(report.getReportKey())) {
                return "Report key already exists: " + report.getReportKey();
            }
            systemReportService.save(report);
            return "Report created from template successfully";
        } catch (Exception e) {
            log.error("Failed to create report from template", e);
            return "Failed to create report: " + e.getMessage();
        }
    }

    @Override
    public String scheduleReport(Long templateId, Map<String, Object> config) {
        try {
            var templateOpt = reportDesignerService.findById(templateId);
            String reportName = templateOpt.map(SysReportTemplate::getTemplateName).orElse("Report_" + templateId);
            String defaultFormat = templateOpt.map(SysReportTemplate::getOutputFormat).orElse("EXCEL");

            String cronExpression = (String) config.get("cronExpression");
            String recipients = (String) config.get("recipients");
            String subject = (String) config.get("subject");
            String body = (String) config.get("body");
            String format = (String) config.getOrDefault("format", defaultFormat);
            String params = (String) config.getOrDefault("params", "{}");

            if (cronExpression == null || cronExpression.isEmpty()) {
                return "Cron expression is required";
            }
            if (recipients == null || recipients.isEmpty()) {
                return "Email recipients are required";
            }

            String jobName = "report_" + templateId;
            String jobGroup = "reports";

            Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName("com.pd.modules.system.report.job.ReportScheduleJob");
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
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

            scheduler.deleteJob(new JobKey(jobName, jobGroup));
            scheduler.scheduleJob(jobDetail, trigger);

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("jobName", jobName);
            result.put("jobGroup", jobGroup);
            result.put("cronExpression", cronExpression);
            result.put("nextFireTime", trigger.getNextFireTime());
            return "Report scheduled successfully";
        } catch (Exception e) {
            log.error("Failed to schedule report", e);
            return "Failed to schedule report: " + e.getMessage();
        }
    }

    @Override
    public String unscheduleReport(Long reportId) {
        try {
            var reportOpt = systemReportService.findById(reportId);
            if (reportOpt.isEmpty()) {
                return "Report not found";
            }
            String jobName = "report_" + reportId;
            String jobGroup = "reports";
            scheduler.deleteJob(new JobKey(jobName, jobGroup));

            SysReport report = reportOpt.get();
            report.setScheduleEnabled(false);
            report.setScheduleCron(null);
            systemReportService.save(report);
            return "Report unscheduled successfully";
        } catch (Exception e) {
            log.error("Failed to unschedule report", e);
            return "Failed to unschedule report: " + e.getMessage();
        }
    }

    @Override
    public List<?> listReportDesignerTemplates(boolean allVersions) {
        return allVersions ? reportDesignerService.findAllVersions() : reportDesignerService.findAll();
    }

    @Override
    public Object getReportDesignerTemplate(Long templateId) {
        return reportDesignerService.findById(templateId).orElse(null);
    }

    @Override
    public Object getReportDesignerTemplateByKey(String templateKey) {
        return reportDesignerService.findByTemplateKey(templateKey).orElse(null);
    }

    @Override
    public String addReportDesignerTemplate(SysReportTemplate sysTemplate) {
        reportDesignerService.save(sysTemplate);
        return "Template added successfully";
    }

    @Override
    public String updateReportDesignerTemplate(SysReportTemplate sysTemplate) {
        var existing = reportDesignerService.findById(sysTemplate.getTemplateId());
        if (existing.isEmpty()) {
            return "Template not found";
        }
        reportDesignerService.save(sysTemplate);
        return "Template updated successfully";
    }

    @Override
    public String deleteReportDesignerTemplate(Long templateId) {
        if (reportDesignerService.deleteById(templateId)) {
            return "Template deleted successfully";
        }
        return "Failed to delete template";
    }

    @Override
    public List<?> getReportDesignerTemplateVersions(String templateKey) {
        return reportDesignerService.findByTemplateKeyOrderByVersionDesc(templateKey);
    }

    @Override
    public List<?> getReportDesignerActiveVersions() {
        return reportDesignerService.findAll().stream()
                .filter(t -> "0".equals(t.getStatus()))
                .map(t -> {
                    Map<String, Object> info = new java.util.HashMap<>();
                    info.put("templateId", t.getTemplateId());
                    info.put("templateKey", t.getTemplateKey());
                    info.put("version", t.getVersion());
                    info.put("status", t.getStatus());
                    return info;
                })
                .toList();
    }

    @Override
    public List<?> getReportDesignerActiveTemplates() {
        return reportDesignerService.findAll().stream()
                .filter(t -> "0".equals(t.getStatus()))
                .toList();
    }

    @Override
    public String archiveReportDesignerTemplate(Long templateId) {
        var templateOpt = reportDesignerService.findById(templateId);
        if (templateOpt.isEmpty()) {
            return "Template not found";
        }
        SysReportTemplate template = templateOpt.get();
        template.setStatus("2");
        reportDesignerService.save(template);
        return "Template archived successfully";
    }

    @Override
    public String activateReportDesignerTemplate(Long templateId) {
        var templateOpt = reportDesignerService.findById(templateId);
        if (templateOpt.isEmpty()) {
            return "Template not found";
        }
        SysReportTemplate template = templateOpt.get();

        var allByKey = reportDesignerService.findByTemplateKeyOrderByVersionDesc(template.getTemplateKey());
        for (SysReportTemplate t : allByKey) {
            t.setStatus("1");
            reportDesignerService.save(t);
        }

        template.setStatus("0");
        reportDesignerService.save(template);
        return "Template activated successfully";
    }

    @Override
    public List<Map<String, Object>> getDatasourceTables(String datasourceKey) {
        return reportDesignerService.getDatasourceTables(datasourceKey);
    }

    @Override
    public List<Map<String, Object>> executeReportDesignerTemplate(Long templateId, Map<String, Object> params) {
        try {
            String paramsJson = params != null ? new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(params) : "{}";
            return reportDesignerService.executeTemplate(templateId, paramsJson);
        } catch (Exception e) {
            log.error("Failed to execute report designer template", e);
            throw new RuntimeException("Failed to execute template: " + e.getMessage());
        }
    }
}
