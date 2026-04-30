package com.pd.gateway.quartz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import com.pd.modules.quartz.service.ISysJobService;

/**
 * Scheduled job controller
 */
@RestController
@RequestMapping("/api/system/job")
public class SysJobController extends BaseController {

    @Autowired
    private SysJobRepository jobRepository;

    @Autowired
    private ISysJobService sysJobService;

    /**
     * Get list of jobs
     */
    @GetMapping("/list")
    public AjaxResult list(SysJob job) {
        return success(jobRepository.findAllActive());
    }

    /**
     * Get job by ID
     */
    @GetMapping("/{jobId}")
    public AjaxResult getInfo(@PathVariable Long jobId) {
        return jobRepository.findById(jobId)
                .map(this::success)
                .orElse(error("Job not found"));
    }

    /**
     * Insert job
     */
    @PostMapping
    public AjaxResult add(@RequestBody SysJob job) throws SchedulerException {
        sysJobService.insertJob(job);
        return success("Job added successfully");
    }

    /**
     * Update job
     */
    @PutMapping
    public AjaxResult edit(@RequestBody SysJob job) throws SchedulerException {
        sysJobService.updateJob(job);
        return success("Job updated successfully");
    }

    /**
     * Delete job
     */
    @DeleteMapping("/{jobId}")
    public AjaxResult remove(@PathVariable Long jobId) throws SchedulerException {
        sysJobService.deleteJobByIds(new Long[]{jobId});
        return success("Job deleted successfully");
    }

    /**
     * Delete jobs in bulk
     */
    @DeleteMapping("/batch")
    public AjaxResult batchRemove(@RequestBody Long[] ids) throws SchedulerException {
        sysJobService.deleteJobByIds(ids);
        return success("Deleted " + ids.length + " job(s)");
    }

    /**
     * Change job status
     */
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysJob job) throws SchedulerException {
        sysJobService.changeStatus(job);
        return success("Job status updated successfully");
    }

    /**
     * Run job immediately
     */
    @PostMapping("/run")
    public AjaxResult run(@RequestBody java.util.Map<String, Object> request) throws SchedulerException {
        // Extract jobId and optional params from request
        Long jobId = request.get("jobId") != null ? ((Number) request.get("jobId")).longValue() : null;
        String overrideParams = request.get("params") != null ? (String) request.get("params") : null;
        String overrideEmailParams = request.get("emailTemplateParams") != null ? (String) request.get("emailTemplateParams") : null;

        if (jobId == null) {
            return error("Job ID is required");
        }

        // Fetch job from database
        SysJob job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));

        // Use override params for this run only (don't save to database)
        final String originalParams = job.getReportParams();
        final String originalEmailParams = job.getEmailTemplateParams();
        if (overrideParams != null && !overrideParams.trim().isEmpty()) {
            job.setReportParams(overrideParams);
        }
        
        // Use override email template params for this run only
        if (overrideEmailParams != null && !overrideEmailParams.trim().isEmpty()) {
            job.setEmailTemplateParams(overrideEmailParams);
        }

        try {
            Long jobLogId = sysJobService.run(job);
            return success(jobLogId);
        } finally {
            // Restore original params so override doesn't persist
            job.setReportParams(originalParams);
        }
    }

    /**
     * Pause job
     */
    @PutMapping("/pause")
    public AjaxResult pause(@RequestBody SysJob job) throws SchedulerException {
        sysJobService.pauseJob(job);
        return success("Job paused successfully");
    }

    /**
     * Resume job
     */
    @PutMapping("/resume")
    public AjaxResult resume(@RequestBody SysJob job) throws SchedulerException {
        sysJobService.resumeJob(job);
        return success("Job resumed successfully");
    }

    /**
     * Pause jobs in bulk
     */
    @PutMapping("/batch/pause")
    public AjaxResult batchPause(@RequestBody Long[] ids) throws SchedulerException {
        for (Long id : ids) {
            SysJob job = jobRepository.findById(id).orElse(null);
            if (job != null) {
                job.setStatus("1");
                sysJobService.pauseJob(job);
            }
        }
        return success("Paused " + ids.length + " job(s)");
    }

    /**
     * Resume jobs in bulk
     */
    @PutMapping("/batch/resume")
    public AjaxResult batchResume(@RequestBody Long[] ids) throws SchedulerException {
        for (Long id : ids) {
            SysJob job = jobRepository.findById(id).orElse(null);
            if (job != null) {
                job.setStatus("0");
                sysJobService.resumeJob(job);
            }
        }
        return success("Resumed " + ids.length + " job(s)");
    }

    /**
     * Run jobs in bulk
     */
    @PostMapping("/batch/run")
    public AjaxResult batchRun(@RequestBody Long[] ids) throws SchedulerException {
        int successCount = 0;
        for (Long id : ids) {
            try {
                SysJob job = jobRepository.findById(id).orElse(null);
                if (job != null) {
                    sysJobService.run(job);
                    successCount++;
                }
            } catch (Exception e) {
                // Continue with other jobs
            }
        }
        return success("Executed " + successCount + "/" + ids.length + " job(s)");
    }

    /**
     * Export jobs as JSON
     */
    @GetMapping("/export")
    public AjaxResult export(@RequestParam(required = false) Long[] ids) {
        List<SysJob> jobs;
        if (ids != null && ids.length > 0) {
            jobs = new ArrayList<>();
            for (Long id : ids) {
                jobRepository.findById(id).ifPresent(jobs::add);
            }
        } else {
            jobs = jobRepository.findAllActive();
        }
        return success(jobs);
    }

    /**
     * Import jobs from JSON
     */
    @PostMapping("/import")
    public AjaxResult importJobs(@RequestBody List<SysJob> jobs) throws SchedulerException {
        int successCount = 0;
        for (SysJob job : jobs) {
            try {
                job.setJobId(null); // Reset ID for new job
                sysJobService.insertJob(job);
                successCount++;
            } catch (Exception e) {
                // Continue with other jobs
            }
        }
        return success("Imported " + successCount + "/" + jobs.size() + " job(s)");
    }

    /**
     * Get available job groups
     */
    @GetMapping("/groups")
    public AjaxResult getGroups() {
        List<String> groups = jobRepository.findAllActive().stream()
                .map(SysJob::getJobGroup)
                .distinct()
                .toList();
        return success(groups);
    }

    /**
     * Get full dependency chain for a job
     */
    @GetMapping("/{jobId}/chain")
    public AjaxResult getChain(@PathVariable Long jobId) {
        List<SysJob> chain = sysJobService.getJobDependencyChain(jobId);
        return success(chain);
    }
}
