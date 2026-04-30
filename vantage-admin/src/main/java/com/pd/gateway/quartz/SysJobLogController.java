package com.pd.gateway.quartz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.pd.modules.quartz.domain.SysJobLog;
import com.pd.modules.quartz.service.ISysJobLogService;

/**
 * Scheduled job log controller
 */
@RestController
@RequestMapping("/system/jobLog")
public class SysJobLogController {

    @Autowired
    private ISysJobLogService sysJobLogService;

    /**
     * Get list of job logs
     */
    @GetMapping("/list")
    public List<SysJobLog> list(SysJobLog jobLog) {
        return sysJobLogService.selectJobLogList(jobLog);
    }

    /**
     * Delete job log by ID
     */
    @DeleteMapping("/{jobLogId}")
    public int remove(@PathVariable Long jobLogId) {
        return sysJobLogService.deleteJobLogById(jobLogId);
    }

    /**
     * Delete job logs by IDs
     */
    @DeleteMapping
    public int batchRemove(@RequestBody Long[] jobLogIds) {
        return sysJobLogService.deleteJobLogByIds(jobLogIds);
    }

    /**
     * Clean all job logs
     */
    @DeleteMapping("/clean")
    public void clean() {
        sysJobLogService.cleanJobLog();
    }
}
