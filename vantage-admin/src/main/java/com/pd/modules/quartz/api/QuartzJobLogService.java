package com.pd.modules.quartz.api;

import com.pd.modules.quartz.api.dto.JobLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QuartzJobLogService {

    List<JobLogDTO> findAll();

    List<JobLogDTO> findRecent(int limit);

    List<JobLogDTO> findByJobName(String jobName);

    List<JobLogDTO> findByStatus(String status);

    Page<JobLogDTO> findByConditionPaginated(String jobName, String jobGroup, String status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    Optional<JobLogDTO> findById(Long jobLogId);

    boolean deleteByIds(Long[] jobLogIds);

    void cleanLogs();

    long count();

    long countByStatus(String status);

    Map<String, Object> getJobMetrics(String jobName);
}
