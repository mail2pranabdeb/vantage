package com.pd.modules.quartz.controller;

import java.util.List;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;

/**
 * Scheduled job controller
 */
@RestController
@RequestMapping("/api/system/job")
public class SysJobController extends BaseController {

    @Autowired
    private SysJobRepository jobRepository;

    @Autowired
    private com.pd.modules.quartz.service.ISysJobService sysJobService;

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
    public AjaxResult run(@RequestBody SysJob job) throws SchedulerException {
        sysJobService.run(job);
        return success("Job executed successfully");
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
}
