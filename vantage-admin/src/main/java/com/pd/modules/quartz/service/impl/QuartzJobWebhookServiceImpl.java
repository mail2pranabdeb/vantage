package com.pd.modules.quartz.service.impl;

import com.pd.modules.quartz.api.QuartzJobService;
import com.pd.modules.quartz.api.QuartzJobWebhookService;
import com.pd.modules.quartz.api.dto.JobDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QuartzJobWebhookServiceImpl implements QuartzJobWebhookService {

    private final QuartzJobService quartzJobService;

    public QuartzJobWebhookServiceImpl(QuartzJobService quartzJobService) {
        this.quartzJobService = quartzJobService;
    }

    @Override
    public String triggerJobByWebhook(Long jobId, String token) {
        Optional<JobDTO> jobOpt = quartzJobService.findById(jobId);

        if (jobOpt.isEmpty()) {
            return "Job not found";
        }

        JobDTO job = jobOpt.get();

        if (job.getWebhookToken() == null || job.getWebhookToken().isEmpty()) {
            return "Webhook not enabled for this job";
        }

        if (!job.getWebhookToken().equals(token)) {
            return "Invalid webhook token";
        }

        quartzJobService.runJob(jobId);
        return "Job triggered successfully";
    }
}
