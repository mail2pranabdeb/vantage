package com.pd.modules.quartz.service;

import com.pd.modules.quartz.domain.SysJob;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service for job templates - pre-defined job configurations
 */
@Service
public class JobTemplateService {

    /**
     * Get all available job templates
     */
    public List<JobTemplate> getTemplates() {
        return Arrays.asList(
            new JobTemplate(
                "daily-backup",
                "Daily Backup",
                "system",
                "backupService.performDailyBackup()",
                "0 2 * * * ?",
                "Daily backup at 2 AM",
                3,
                60,
                7200
            ),
            new JobTemplate(
                "hourly-cleanup",
                "Hourly Cleanup",
                "system",
                "cleanupService.performCleanup()",
                "0 0 * * * ?",
                "Hourly cleanup task",
                2,
                30,
                1800
            ),
            new JobTemplate(
                "weekly-report",
                "Weekly Report Generation",
                "report",
                "reportService.generateWeeklyReport()",
                "0 0 8 ? * MON",
                "Weekly report every Monday 8 AM",
                3,
                120,
                3600
            ),
            new JobTemplate(
                "monthly-analytics",
                "Monthly Analytics",
                "analytics",
                "analyticsService.generateMonthlyReport()",
                "0 0 6 1 * ?",
                "Monthly analytics on 1st day 6 AM",
                5,
                300,
                7200
            ),
            new JobTemplate(
                "cache-refresh",
                "Cache Refresh",
                "system",
                "cacheService.refreshAllCaches()",
                "0 */30 * * * ?",
                "Refresh cache every 30 minutes",
                1,
                10,
                300
            ),
            new JobTemplate(
                "notification-check",
                "Notification Checker",
                "notification",
                "notificationService.checkAndSendNotifications()",
                "0 */15 * * * ?",
                "Check and send notifications every 15 minutes",
                2,
                30,
                600
            )
        );
    }

    /**
     * Get template by name
     */
    public Optional<JobTemplate> getTemplateByName(String name) {
        return getTemplates().stream()
                .filter(t -> t.name.equals(name))
                .findFirst();
    }

    /**
     * Create a job from template
     */
    public SysJob createJobFromTemplate(String templateName, String customJobName) {
        Optional<JobTemplate> templateOpt = getTemplateByName(templateName);
        if (templateOpt.isEmpty()) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        JobTemplate template = templateOpt.get();
        SysJob job = new SysJob();
        job.setJobName(customJobName != null ? customJobName : template.jobName);
        job.setJobGroup(template.jobGroup);
        job.setInvokeTarget(template.invokeTarget);
        job.setCronExpression(template.cronExpression);
        job.setRemark(template.description);
        job.setMaxRetryCount(template.maxRetryCount);
        job.setRetryInterval(template.retryInterval);
        job.setTimeoutSeconds(template.timeoutSeconds);
        job.setStatus("0");
        job.setTemplateName(template.name);
        job.setConcurrent("0");
        job.setMisfirePolicy("3");

        return job;
    }

    /**
     * Job template record
     */
    public static record JobTemplate(
        String name,
        String jobName,
        String jobGroup,
        String invokeTarget,
        String cronExpression,
        String description,
        Integer maxRetryCount,
        Integer retryInterval,
        Integer timeoutSeconds
    ) {}
}
