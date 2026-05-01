package com.pd.modules.quartz.api;

import com.pd.modules.quartz.api.dto.JobLogDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Quartz module public API for job log operations.
 */
public interface QuartzJobLogService {

    List<JobLogDTO> findAll();

    List<JobLogDTO> findRecent(int limit);

    List<JobLogDTO> findByJobName(String jobName);

    List<JobLogDTO> findByStatus(String status);

    Optional<JobLogDTO> findById(Long jobLogId);

    boolean deleteByIds(Long[] jobLogIds);

    void cleanLogs();

    long count();

    long countByStatus(String status);

    Map<String, Object> getJobMetrics(String jobName);
}
