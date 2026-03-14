package com.pd.common.event.job;

import com.pd.common.event.DomainEvent;

/**
 * Event published when a scheduled job is created.
 * Other modules can listen to this event for cross-module operations.
 */
public class JobCreatedEvent extends DomainEvent {

    private final Long jobId;
    private final String jobName;
    private final String jobGroup;

    public JobCreatedEvent(Long jobId, String jobName, String jobGroup) {
        super("JOB_CREATED");
        this.jobId = jobId;
        this.jobName = jobName;
        this.jobGroup = jobGroup;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }
}
