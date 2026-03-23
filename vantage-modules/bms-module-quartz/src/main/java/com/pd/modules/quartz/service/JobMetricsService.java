package com.pd.modules.quartz.service;

import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobLogRepository;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for job dashboard metrics and statistics
 */
@Service
public class JobMetricsService {

    private final SysJobRepository jobRepository;
    private final SysJobLogRepository jobLogRepository;

    public JobMetricsService(SysJobRepository jobRepository, SysJobLogRepository jobLogRepository) {
        this.jobRepository = jobRepository;
        this.jobLogRepository = jobLogRepository;
    }

    /**
     * Get dashboard metrics
     */
    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Job counts
        List<SysJob> allJobs = jobRepository.findAll();
        long totalJobs = allJobs.size();
        long activeJobs = allJobs.stream().filter(j -> "0".equals(j.getStatus())).count();
        long pausedJobs = totalJobs - activeJobs;

        // Get last 30 days statistics
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        SysJobLogRepository.JobLogStatistics stats = jobLogRepository.getStatistics(startDate, LocalDateTime.now());

        metrics.put("totalJobs", totalJobs);
        metrics.put("activeJobs", activeJobs);
        metrics.put("pausedJobs", pausedJobs);
        metrics.put("totalExecutions", stats.getTotalExecutions());
        metrics.put("successfulExecutions", stats.getSuccessfulExecutions());
        metrics.put("failedExecutions", stats.getFailedExecutions());
        metrics.put("successRate", stats.getSuccessRate());
        metrics.put("avgExecutionDuration", stats.getAvgExecutionDuration());

        // Recent failed jobs
        metrics.put("recentFailures", jobLogRepository.findRecentFailed(5).stream()
                .map(log -> {
                    Map<String, Object> failure = new HashMap<>();
                    failure.put("jobName", log.getJobName());
                    failure.put("jobGroup", log.getJobGroup());
                    failure.put("startTime", log.getStartTime());
                    failure.put("message", log.getJobMessage());
                    return failure;
                })
                .toList());

        // Jobs by group
        Map<String, Long> jobsByGroup = allJobs.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        SysJob::getJobGroup,
                        java.util.stream.Collectors.counting()));
        metrics.put("jobsByGroup", jobsByGroup);

        return metrics;
    }

    /**
     * Get execution trend data for charts
     */
    public List<Map<String, Object>> getExecutionTrend(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        return jobLogRepository.findAll().stream()
                .filter(log -> log.getStartTime() != null && 
                        log.getStartTime().isAfter(startDate) && 
                        log.getStartTime().isBefore(endDate))
                .collect(java.util.stream.Collectors.groupingBy(
                        log -> log.getStartTime().toLocalDate().toString()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", entry.getKey());
                    data.put("total", entry.getValue().size());
                    data.put("success", entry.getValue().stream()
                            .filter(log -> "0".equals(log.getStatus())).count());
                    data.put("failed", entry.getValue().stream()
                            .filter(log -> "1".equals(log.getStatus())).count());
                    data.put("avgDuration", entry.getValue().stream()
                            .mapToLong(log -> log.getExecutionDuration() != null ? log.getExecutionDuration() : 0)
                            .average()
                            .orElse(0.0));
                    return data;
                })
                .toList();
    }
}
