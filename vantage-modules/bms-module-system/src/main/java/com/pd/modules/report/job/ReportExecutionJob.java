package com.pd.modules.report.job;

import com.pd.modules.report.service.SysReportService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Quartz job for executing reports on schedule
 */
@Component
public class ReportExecutionJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(ReportExecutionJob.class);

    @Autowired
    private SysReportService reportService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Long reportId = context.getMergedJobDataMap().getLong("reportId");
            String params = context.getMergedJobDataMap().getString("params");
            
            log.info("Executing scheduled report: {}", reportId);
            
            // Execute report
            reportService.executeAndEmail(reportId);
            
            log.info("Scheduled report {} executed successfully", reportId);
        } catch (Exception e) {
            log.error("Failed to execute scheduled report", e);
            throw new JobExecutionException(e);
        }
    }
}
