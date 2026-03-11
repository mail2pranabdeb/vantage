package com.pd.modules.quartz.controller;

import java.util.List;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.service.ISysJobService;

/**
 * Scheduled job controller
 */
@RestController
@RequestMapping("/system/job")
public class SysJobController {

    @Autowired
    private ISysJobService sysJobService;

    /**
     * Get list of jobs
     */
    @GetMapping("/list")
    public List<SysJob> list(SysJob job) {
        return sysJobService.selectJobList(job);
    }

    /**
     * Get job by ID
     */
    @GetMapping("/{jobId}")
    public SysJob getInfo(@PathVariable Long jobId) {
        return sysJobService.selectJobById(jobId);
    }

    /**
     * Insert job
     */
    @PostMapping
    public int add(@RequestBody SysJob job) throws SchedulerException {
        return sysJobService.insertJob(job);
    }

    /**
     * Update job
     */
    @PutMapping
    public int edit(@RequestBody SysJob job) throws SchedulerException {
        return sysJobService.updateJob(job);
    }

    /**
     * Delete job
     */
    @DeleteMapping("/{jobIds}")
    public void remove(@PathVariable Long[] jobIds) throws SchedulerException {
        sysJobService.deleteJobByIds(jobIds);
    }

    /**
     * Change job status
     */
    @PutMapping("/changeStatus")
    public int changeStatus(@RequestBody SysJob job) throws SchedulerException {
        return sysJobService.changeStatus(job);
    }

    /**
     * Run job immediately
     */
    @PutMapping("/run")
    public void run(@RequestBody SysJob job) throws SchedulerException {
        sysJobService.run(job);
    }

    /**
     * Pause job
     */
    @PutMapping("/pause")
    public void pause(@RequestBody SysJob job) throws SchedulerException {
        sysJobService.pauseJob(job);
    }

    /**
     * Resume job
     */
    @PutMapping("/resume")
    public void resume(@RequestBody SysJob job) throws SchedulerException {
        sysJobService.resumeJob(job);
    }
}
