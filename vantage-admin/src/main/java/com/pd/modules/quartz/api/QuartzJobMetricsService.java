package com.pd.modules.quartz.api;

import java.util.List;
import java.util.Map;

/**
 * Quartz module public API for job dashboard metrics.
 */
public interface QuartzJobMetricsService {

    Map<String, Object> getDashboardMetrics();

    List<Map<String, Object>> getExecutionTrend(int days);

    Map<String, Object> getJobHealth();
}
