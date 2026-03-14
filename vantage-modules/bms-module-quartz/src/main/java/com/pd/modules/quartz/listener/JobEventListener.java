package com.pd.modules.quartz.listener;

import com.pd.common.event.job.JobCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener for job events within the quartz module.
 * Can be extended to perform additional operations when job events occur.
 */
@Component
public class JobEventListener {

    private static final Logger log = LoggerFactory.getLogger(JobEventListener.class);

    /**
     * Handle job created event after transaction commits.
     * Other modules can use this pattern to react to job creation.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJobCreated(JobCreatedEvent event) {
        log.info("Job created event received: jobId={}, jobName={}, jobGroup={}", 
            event.getJobId(), event.getJobName(), event.getJobGroup());
        
        // Example: Could notify other systems, log to external monitoring, etc.
        // This is where cross-module communication happens without direct dependencies
    }
}
