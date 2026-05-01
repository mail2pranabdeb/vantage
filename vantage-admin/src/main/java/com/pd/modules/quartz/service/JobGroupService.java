package com.pd.modules.quartz.service;

import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobGroupService {

    private static final Logger log = LoggerFactory.getLogger(JobGroupService.class);

    @Autowired
    private SysJobRepository jobRepository;

    @Autowired
    private ISysJobService jobService;

    @Autowired
    private Scheduler scheduler;

    /**
     * Get all distinct job groups
     */
    public List<String> getAllJobGroups() {
        return jobRepository.findAll().stream()
                .map(SysJob::getJobGroup)
                .filter(group -> group != null && !group.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get jobs in a specific group
     */
    public List<SysJob> getJobsInGroup(String jobGroup) {
        return jobRepository.findAll().stream()
                .filter(job -> jobGroup.equals(job.getJobGroup()))
                .collect(Collectors.toList());
    }

    /**
     * Execute all jobs in a group sequentially
     * @return List of execution results with jobId, status, message
     */
    public List<Map<String, Object>> executeGroup(String jobGroup) {
        List<SysJob> jobs = getJobsInGroup(jobGroup);
        List<Map<String, Object>> results = new ArrayList<>();

        log.info("Executing job group '{}' with {} jobs", jobGroup, jobs.size());

        for (SysJob job : jobs) {
            try {
                // Only execute active jobs
                if (!"0".equals(job.getStatus())) {
                    results.add(Map.of(
                        "jobId", job.getJobId(),
                        "jobName", job.getJobName(),
                        "status", "SKIPPED",
                        "message", "Job is not active"
                    ));
                    continue;
                }

                Long logId = jobService.run(job);
                results.add(Map.of(
                    "jobId", job.getJobId(),
                    "jobName", job.getJobName(),
                    "status", "SUCCESS",
                    "message", "Job triggered, Log ID: " + logId
                ));
                log.info("Triggered job: {} (ID: {}) in group {}", job.getJobName(), job.getJobId(), jobGroup);
                
                // Small delay between jobs
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("Failed to execute job {} in group {}", job.getJobName(), jobGroup, e);
                results.add(Map.of(
                    "jobId", job.getJobId(),
                    "jobName", job.getJobName(),
                    "status", "FAILED",
                    "message", e.getMessage()
                ));
            }
        }

        log.info("Completed job group '{}', {} jobs executed", jobGroup, results.size());
        return results;
    }

    /**
     * Get summary of job groups (count of jobs per group)
     */
    public List<Map<String, Object>> getJobGroupSummary() {
        List<String> groups = getAllJobGroups();
        List<Map<String, Object>> summary = new ArrayList<>();
        
        for (String group : groups) {
            List<SysJob> jobsInGroup = getJobsInGroup(group);
            long total = jobsInGroup.size();
            long active = jobsInGroup.stream().filter(j -> "0".equals(j.getStatus())).count();
            
            Map<String, Object> groupInfo = new java.util.HashMap<>();
            groupInfo.put("jobGroup", group);
            groupInfo.put("totalJobs", total);
            groupInfo.put("activeJobs", active);
            summary.add(groupInfo);
        }
        
        return summary;
    }
}