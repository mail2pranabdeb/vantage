package com.pd.common.event;

import java.time.LocalDateTime;

/**
 * Base class for domain events in the application.
 * Events are used for loose coupling between modules.
 */
public abstract class DomainEvent {
    
    private final LocalDateTime occurredOn;
    private final String eventType;

    protected DomainEvent(String eventType) {
        this.occurredOn = LocalDateTime.now();
        this.eventType = eventType;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    public String getEventType() {
        return eventType;
    }
}
