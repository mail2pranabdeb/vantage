package com.pd.modules.quartz.api;

import com.pd.modules.quartz.api.dto.JobDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface QuartzJobService {

    List<JobDTO> findAll();

    Page<JobDTO> searchJobs(String jobName, String jobGroup, String status, Pageable pageable);

    Optional<JobDTO> findById(Long jobId);

    JobDTO createJob(JobDTO job);

    JobDTO updateJob(JobDTO job);

    boolean deleteJob(Long jobId);

    void runJob(Long jobId);

    void pauseJob(Long jobId);

    void resumeJob(Long jobId);
}
