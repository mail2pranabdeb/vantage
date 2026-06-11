package com.pd.framework.config;

import com.pd.common.annotation.Audited;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AuditHibernateConfig {

    private static final Logger log = LoggerFactory.getLogger(AuditHibernateConfig.class);

    @PersistenceUnit
    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        SessionFactoryImplementor sessionFactory = emf.unwrap(SessionFactoryImplementor.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry()
                .getService(EventListenerRegistry.class);
        registry.prependListeners(EventType.PRE_UPDATE, (PreUpdateEventListener) this::onPreUpdate);
        registry.prependListeners(EventType.PRE_DELETE, (PreDeleteEventListener) this::onPreDelete);
    }

    private boolean onPreUpdate(PreUpdateEvent event) {
        if (!event.getEntity().getClass().isAnnotationPresent(Audited.class)) {
            return false;
        }
        try {
            Object[] oldState = event.getOldState();
            if (oldState == null) return false;

            String[] propertyNames = event.getPersister().getPropertyNames();
            Map<String, Object> beforeMap = new LinkedHashMap<>();
            for (int i = 0; i < propertyNames.length; i++) {
                if (oldState[i] != null) {
                    beforeMap.put(propertyNames[i], oldState[i]);
                }
            }
            AuditContextHolder.setBeforeState(beforeMap);
        } catch (Exception e) {
            log.warn("Failed to capture pre-update state for audit", e);
        }
        return false;
    }

    private boolean onPreDelete(PreDeleteEvent event) {
        if (!event.getEntity().getClass().isAnnotationPresent(Audited.class)) {
            return false;
        }
        try {
            Object[] oldState = event.getDeletedState();
            if (oldState == null) return false;

            String[] propertyNames = event.getPersister().getPropertyNames();
            Map<String, Object> beforeMap = new LinkedHashMap<>();
            for (int i = 0; i < propertyNames.length; i++) {
                if (oldState[i] != null) {
                    beforeMap.put(propertyNames[i], oldState[i]);
                }
            }
            AuditContextHolder.setBeforeState(beforeMap);
        } catch (Exception e) {
            log.warn("Failed to capture pre-delete state for audit", e);
        }
        return false;
    }
}
