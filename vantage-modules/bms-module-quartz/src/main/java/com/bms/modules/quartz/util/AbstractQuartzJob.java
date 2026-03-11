package com.pd.modules.quartz.util;

import java.time.LocalDateTime;
import java.util.Date;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.domain.SysJobLog;

/**
 * Abstract Quartz job execution class
 */
public abstract class AbstractQuartzJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SysJob sysJob = (SysJob) context.getMergedJobDataMap().get("TASK_PROPERTIES");
        Long jobId = sysJob.getJobId();
        String jobGroup = sysJob.getJobGroup();
        
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobName(sysJob.getJobName());
        jobLog.setJobGroup(jobGroup);
        jobLog.setInvokeTarget(sysJob.getInvokeTarget());
        jobLog.setStartTime(LocalDateTime.now());
        
        long startTime = System.currentTimeMillis();
        
        try {
            runTask(sysJob);
            jobLog.setStatus("0");
            jobLog.setJobMessage("Execution successful");
        } catch (Exception e) {
            log.error("Job execution failed: {}", e.getMessage(), e);
            jobLog.setStatus("1");
            jobLog.setJobMessage("Execution failed: " + e.getMessage());
            jobLog.setExceptionInfo(getStackTrace(e));
        } finally {
            long endTime = System.currentTimeMillis();
            jobLog.setEndTime(LocalDateTime.now());
            long time = endTime - startTime;
            log.info("Job {} executed in {} ms", jobId, time);
        }
    }

    /**
     * Execute task method
     */
    protected abstract void runTask(SysJob sysJob) throws Exception;

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
