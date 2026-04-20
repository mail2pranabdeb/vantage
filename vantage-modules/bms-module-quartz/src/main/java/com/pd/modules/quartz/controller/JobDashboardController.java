package com.pd.modules.quartz.controller;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.quartz.service.JobMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Job dashboard and metrics controller
 */
@RestController
@RequestMapping("/api/system/job-dashboard")
public class JobDashboardController extends BaseController {

    @Autowired
    private JobMetricsService jobMetricsService;

    /**
     * Get dashboard metrics
     */
    @GetMapping("/metrics")
    public AjaxResult getMetrics() {
        Map<String, Object> metrics = jobMetricsService.getDashboardMetrics();
        return success(metrics);
    }

    /**
     * Get execution trend data
     */
    @GetMapping("/trend")
    public AjaxResult getTrend(@RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> trend = jobMetricsService.getExecutionTrend(days);
        return success(trend);
    }

    /**
     * Get job health status (stuck, missed, frequent failures)
     */
    @GetMapping("/health")
    public AjaxResult getHealth() {
        Map<String, Object> health = jobMetricsService.getJobHealth();
        return success(health);
    }
}
