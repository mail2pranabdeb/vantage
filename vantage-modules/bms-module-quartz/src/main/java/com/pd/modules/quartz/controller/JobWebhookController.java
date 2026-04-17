package com.pd.modules.quartz.controller;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import com.pd.modules.quartz.service.ISysJobService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Public Webhook Controller for Job Triggering
 */
@RestController
@RequestMapping("/api/public/job/webhook")
public class JobWebhookController extends BaseController {

    @Autowired
    private SysJobRepository jobRepository;

    @Autowired
    private ISysJobService sysJobService;

    /**
     * Trigger a job via webhook token
     *
     * @param jobId The ID of the job to trigger
     * @param token The security token for the job
     * @return AjaxResult
     */
    @PostMapping("/{jobId}")
    public AjaxResult trigger(@PathVariable Long jobId, @RequestParam String token) throws SchedulerException {
        SysJob job = jobRepository.findById(jobId).orElse(null);
        
        if (job == null) {
            return error("Job not found");
        }
        
        if (job.getWebhookToken() == null || job.getWebhookToken().isEmpty()) {
            return error("Webhook not enabled for this job");
        }
        
        if (!job.getWebhookToken().equals(token)) {
            return error("Invalid webhook token");
        }
        
        Long jobLogId = sysJobService.run(job);
        return success("Job triggered successfully. Log ID: " + jobLogId);
    }
}
