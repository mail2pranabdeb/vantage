package com.pd.modules.quartz.api;

import com.pd.modules.quartz.api.dto.JobDTO;
import java.util.List;
import java.util.Map;

/**
 * Quartz module public API for job group operations.
 */
public interface QuartzJobGroupService {

    List<String> getAllJobGroups();

    List<JobDTO> getJobsInGroup(String jobGroup);

    List<Map<String, Object>> executeGroup(String jobGroup);

    List<Map<String, Object>> getJobGroupSummary();
}
