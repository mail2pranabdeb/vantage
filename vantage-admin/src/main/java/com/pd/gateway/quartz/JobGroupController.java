package com.pd.gateway.quartz;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.quartz.service.JobGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Job Group operations
 */
@RestController
@RequestMapping("/api/system/job-group")
public class JobGroupController extends BaseController {

    @Autowired
    private JobGroupService jobGroupService;

    /**
     * Get all job groups
     */
    @GetMapping("/list")
    public AjaxResult getJobGroups() {
        return success(jobGroupService.getJobGroupSummary());
    }

    /**
     * Get jobs in a specific group
     */
    @GetMapping("/{jobGroup}/jobs")
    public AjaxResult getJobsInGroup(@PathVariable String jobGroup) {
        return success(jobGroupService.getJobsInGroup(jobGroup));
    }

    /**
     * Execute all jobs in a group sequentially
     */
    @PostMapping("/{jobGroup}/execute")
    public AjaxResult executeGroup(@PathVariable String jobGroup) {
        List<Map<String, Object>> results = jobGroupService.executeGroup(jobGroup);
        long successCount = results.stream()
                .filter(r -> "SUCCESS".equals(r.get("status")))
                .count();
        long failedCount = results.stream()
                .filter(r -> "FAILED".equals(r.get("status")))
                .count();
        
        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        
        if (failedCount > 0) {
            response.put("msg", "Group executed. Success: " + successCount + ", Failed: " + failedCount);
            return AjaxResult.success(response);
        }
        response.put("msg", "All jobs executed. Success: " + successCount);
        return AjaxResult.success(response);
    }
}
