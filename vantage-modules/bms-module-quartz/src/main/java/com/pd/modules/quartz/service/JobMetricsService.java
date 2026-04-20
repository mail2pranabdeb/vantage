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

        // Most failed jobs (last 30 days)
        metrics.put("mostFailedJobs", jobLogRepository.findRecentFailed(10).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        log -> log.getJobName() + " (" + log.getJobGroup() + ")",
                        java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> jobStat = new HashMap<>();
                    jobStat.put("jobName", entry.getKey());
                    jobStat.put("failureCount", entry.getValue());
                    return jobStat;
                })
                .toList());

        // Slowest jobs
        metrics.put("slowestJobs", jobLogRepository.findAll().stream()
                .filter(log -> log.getExecutionDuration() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        log -> log.getJobName() + " (" + log.getJobGroup() + ")",
                        java.util.stream.Collectors.averagingLong(log -> log.getExecutionDuration())))
                .entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> jobStat = new HashMap<>();
                    jobStat.put("jobName", entry.getKey());
                    jobStat.put("avgDuration", Math.round(entry.getValue()));
                    return jobStat;
                })
                .toList());

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

    /**
     * Get job health status - detect stuck and missed jobs
     */
    public Map<String, Object> getJobHealth() {
        Map<String, Object> health = new HashMap<>();
        
        List<SysJob> allJobs = jobRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        // Jobs running for too long (stuck) - running more than 1 hour
        List<Map<String, Object>> stuckJobs = jobLogRepository.findAll().stream()
                .filter(log -> log.getEndTime() == null && log.getStartTime() != null)
                .filter(log -> log.getStartTime().isBefore(now.minusHours(1)))
                .map(log -> {
                    Map<String, Object> job = new HashMap<>();
                    job.put("jobName", log.getJobName());
                    job.put("jobGroup", log.getJobGroup());
                    job.put("startTime", log.getStartTime());
                    job.put("runningFor", java.time.Duration.between(log.getStartTime(), now).toMinutes());
                    return job;
                })
                .toList();
        
        // Jobs that haven't run in expected time (based on cron)
        // For simplicity, check jobs not run in the last 24 hours that are active
        List<Map<String, Object>> missedJobs = allJobs.stream()
                .filter(job -> "0".equals(job.getStatus()))
                .filter(job -> job.getCronExpression() != null && !job.getCronExpression().isEmpty())
                .filter(job -> {
                    // Check last execution time from logs
                    return !jobLogRepository.findAll().stream()
                            .filter(log -> log.getJobName().equals(job.getJobName()))
                            .anyMatch(log -> log.getStartTime() != null && log.getStartTime().isAfter(now.minusHours(24)));
                })
                .map(job -> {
                    Map<String, Object> jobInfo = new HashMap<>();
                    jobInfo.put("jobName", job.getJobName());
                    jobInfo.put("jobGroup", job.getJobGroup());
                    jobInfo.put("cronExpression", job.getCronExpression());
                    jobInfo.put("lastExpected", "within 24 hours");
                    return jobInfo;
                })
                .toList();
        
        // Jobs with frequent failures
        List<Map<String, Object>> frequentFailures = allJobs.stream()
                .filter(job -> job.getJobName() != null)
                .map(job -> {
                    long failures = jobLogRepository.findAll().stream()
                            .filter(log -> log.getJobName().equals(job.getJobName()))
                            .filter(log -> log.getStartTime() != null && log.getStartTime().isAfter(now.minusDays(7)))
                            .filter(log -> "1".equals(log.getStatus()))
                            .count();
                    if (failures >= 3) {
                        Map<String, Object> jobStat = new HashMap<>();
                        jobStat.put("jobName", job.getJobName());
                        jobStat.put("jobGroup", job.getJobGroup());
                        jobStat.put("failuresInWeek", failures);
                        return jobStat;
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        
        health.put("stuckJobs", stuckJobs);
        health.put("missedJobs", missedJobs);
        health.put("frequentFailures", frequentFailures);
        health.put("healthStatus", stuckJobs.isEmpty() && frequentFailures.size() < 3 ? "healthy" : "warning");
        
        return health;
    }
}
