package com.pd.modules.quartz.service.impl;

import com.pd.modules.quartz.api.QuartzJobMetricsService;
import com.pd.modules.quartz.service.JobMetricsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QuartzJobMetricsServiceImpl implements QuartzJobMetricsService {

    private final JobMetricsService jobMetricsService;

    public QuartzJobMetricsServiceImpl(JobMetricsService jobMetricsService) {
        this.jobMetricsService = jobMetricsService;
    }

    @Override
    public Map<String, Object> getDashboardMetrics() {
        return jobMetricsService.getDashboardMetrics();
    }

    @Override
    public List<Map<String, Object>> getExecutionTrend(int days) {
        return jobMetricsService.getExecutionTrend(days);
    }

    @Override
    public Map<String, Object> getJobHealth() {
        return jobMetricsService.getJobHealth();
    }
}
