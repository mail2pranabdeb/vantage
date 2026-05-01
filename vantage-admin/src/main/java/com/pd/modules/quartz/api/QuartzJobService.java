package com.pd.modules.quartz.api;

import com.pd.modules.quartz.api.dto.JobDTO;
import java.util.List;
import java.util.Optional;

public interface QuartzJobService {

    List<JobDTO> findAll();

    Optional<JobDTO> findById(Long jobId);

    JobDTO createJob(JobDTO job);

    JobDTO updateJob(JobDTO job);

    boolean deleteJob(Long jobId);

    void runJob(Long jobId);

    void pauseJob(Long jobId);

    void resumeJob(Long jobId);
}
