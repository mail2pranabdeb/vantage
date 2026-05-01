package com.pd.modules.quartz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending real-time job status updates via WebSocket
 */
@Service
public class JobWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(JobWebSocketService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    /**
     * Send job execution started event
     */
    public void sendJobStarted(Long jobId, String jobName) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "JOB_STARTED");
        event.put("jobId", jobId);
        event.put("jobName", jobName);
        event.put("timestamp", LocalDateTime.now().toString());
        
        sendMessage("/topic/job/updates", event);
        log.info("Sent JOB_STARTED event for job {}", jobId);
    }

    /**
     * Send job execution completed event
     */
    public void sendJobCompleted(Long jobId, String jobName, String status, Long duration) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "JOB_COMPLETED");
        event.put("jobId", jobId);
        event.put("jobName", jobName);
        event.put("status", status);
        event.put("duration", duration);
        event.put("timestamp", LocalDateTime.now().toString());
        
        sendMessage("/topic/job/updates", event);
        log.info("Sent JOB_COMPLETED event for job {}", jobId);
    }

    /**
     * Send job failed event
     */
    public void sendJobFailed(Long jobId, String jobName, String errorMessage) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "JOB_FAILED");
        event.put("jobId", jobId);
        event.put("jobName", jobName);
        event.put("errorMessage", errorMessage);
        event.put("timestamp", LocalDateTime.now().toString());
        
        sendMessage("/topic/job/updates", event);
        log.info("Sent JOB_FAILED event for job {}", jobId);
    }

    /**
     * Send job status changed event
     */
    public void sendJobStatusChanged(Long jobId, String jobName, String oldStatus, String newStatus) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "JOB_STATUS_CHANGED");
        event.put("jobId", jobId);
        event.put("jobName", jobName);
        event.put("oldStatus", oldStatus);
        event.put("newStatus", newStatus);
        event.put("timestamp", LocalDateTime.now().toString());
        
        sendMessage("/topic/job/updates", event);
        log.info("Sent JOB_STATUS_CHANGED event for job {}", jobId);
    }

    /**
     * Send job created event
     */
    public void sendJobCreated(Long jobId, String jobName) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "JOB_CREATED");
        event.put("jobId", jobId);
        event.put("jobName", jobName);
        event.put("timestamp", LocalDateTime.now().toString());
        
        sendMessage("/topic/job/updates", event);
        log.info("Sent JOB_CREATED event for job {}", jobId);
    }

    /**
     * Send job deleted event
     */
    public void sendJobDeleted(Long jobId, String jobName) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "JOB_DELETED");
        event.put("jobId", jobId);
        event.put("jobName", jobName);
        event.put("timestamp", LocalDateTime.now().toString());
        
        sendMessage("/topic/job/updates", event);
        log.info("Sent JOB_DELETED event for job {}", jobId);
    }

    /**
     * Send message to WebSocket topic
     */
    private void sendMessage(String destination, Map<String, Object> message) {
        try {
            messagingTemplate.convertAndSend(destination, (Object) message);
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to {}", destination, e);
        }
    }
}
