package com.pd.modules.quartz.api;

import com.pd.modules.quartz.api.dto.JobDTO;

/**
 * Quartz module public API for job webhook trigger.
 */
public interface QuartzJobWebhookService {

    String triggerJobByWebhook(Long jobId, String token);
}
