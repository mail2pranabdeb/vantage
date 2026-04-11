package com.pd.modules.quartz.util;

import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.domain.SysJobLog;
import com.pd.modules.quartz.infrastructure.repository.SysJobLogRepository;
import com.pd.modules.quartz.service.JobDependencyService;
import com.pd.modules.quartz.service.JobNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * Abstract Quartz job execution class with retry, timeout, and logging support
 */
public abstract class AbstractQuartzJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired(required = false)
    private SysJobLogRepository jobLogRepository;

    @Autowired(required = false)
    private JobNotificationService notificationService;

    @Autowired(required = false)
    private JobDependencyService dependencyService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SysJob sysJob = (SysJob) context.getMergedJobDataMap().get("TASK_PROPERTIES");
        Long jobId = sysJob.getJobId();
        String jobGroup = sysJob.getJobGroup();

        // Initialize job log
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobId(jobId);
        jobLog.setJobName(sysJob.getJobName());
        jobLog.setJobGroup(jobGroup);
        jobLog.setInvokeTarget(sysJob.getInvokeTarget());
        jobLog.setStartTime(LocalDateTime.now());
        jobLog.setRetryCount(0);

        long startTime = System.currentTimeMillis();
        int retryCount = 0;
        int maxRetries = sysJob.getMaxRetryCount() != null ? sysJob.getMaxRetryCount() : 0;
        int timeoutSeconds = sysJob.getTimeoutSeconds() != null ? sysJob.getTimeoutSeconds() : 3600;

        boolean success = false;
        Exception lastException = null;

        // Retry loop
        while (!success && retryCount <= maxRetries) {
            try {
                log.info("Executing job {} (attempt {}/{})", jobId, retryCount + 1, maxRetries + 1);
                
                // Execute with timeout
                executeWithTimeout(sysJob, timeoutSeconds);
                
                success = true;
                jobLog.setStatus("0");
                jobLog.setJobMessage("Execution successful");
                jobLog.setRetryCount(retryCount);
                
            } catch (TimeoutException e) {
                log.error("Job {} timed out after {} seconds", jobId, timeoutSeconds, e);
                lastException = e;
                jobLog.setStatus("1");
                jobLog.setJobMessage("Execution timed out after " + timeoutSeconds + " seconds");
                jobLog.setExceptionInfo(getStackTrace(e));
            } catch (Exception e) {
                log.error("Job {} execution failed (attempt {}/{})", jobId, retryCount + 1, maxRetries + 1, e);
                lastException = e;
                retryCount++;
                
                if (retryCount <= maxRetries) {
                    int retryInterval = sysJob.getRetryInterval() != null ? sysJob.getRetryInterval() : 60;
                    log.info("Retrying job {} in {} seconds...", jobId, retryInterval);
                    try {
                        Thread.sleep(retryInterval * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    jobLog.setStatus("1");
                    jobLog.setJobMessage("Execution failed after " + (maxRetries + 1) + " attempts: " + e.getMessage());
                    jobLog.setExceptionInfo(getStackTrace(e));
                    jobLog.setRetryCount(retryCount);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long executionDuration = endTime - startTime;
        
        jobLog.setEndTime(LocalDateTime.now());
        jobLog.setExecutionDuration(executionDuration);

        // Save job log
        if (jobLogRepository != null) {
            jobLogRepository.save(jobLog);
        }

        log.info("Job {} completed in {} ms with status {}", jobId, executionDuration, jobLog.getStatus());

        // Handle post-execution actions
        if (success) {
            // Trigger dependent jobs
            if (dependencyService != null) {
                dependencyService.triggerDependentJobs(sysJob);
            }
        } else {
            // Send failure notifications
            if (notificationService != null && lastException != null) {
                notificationService.sendFailureNotification(sysJob, jobLog);
            }
        }

        // Throw exception if failed after all retries
        if (!success && lastException != null) {
            throw new JobExecutionException(lastException);
        }
    }

    /**
     * Execute task with timeout
     */
    private void executeWithTimeout(SysJob sysJob, int timeoutSeconds) throws Exception {
        Future<?> future = executorService.submit(() -> {
            try {
                runTask(sysJob);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        try {
            future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            throw e;
        }
    }

    /**
     * Execute task method - to be implemented by concrete job classes
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
