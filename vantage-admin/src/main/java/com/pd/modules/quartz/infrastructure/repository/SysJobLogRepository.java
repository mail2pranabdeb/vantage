package com.pd.modules.quartz.infrastructure.repository;

import com.pd.modules.quartz.domain.SysJobLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class SysJobLogRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find all job logs ordered by start time descending
     */
    public List<SysJobLog> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SysJobLog> cq = cb.createQuery(SysJobLog.class);
        Root<SysJobLog> root = cq.from(SysJobLog.class);
        cq.select(root).orderBy(cb.desc(root.get("startTime")));
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Find job logs by condition
     */
    public List<SysJobLog> findByCondition(String jobName, String jobGroup, String status, LocalDateTime startTime, LocalDateTime endTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SysJobLog> cq = cb.createQuery(SysJobLog.class);
        Root<SysJobLog> root = cq.from(SysJobLog.class);
        List<Predicate> predicates = new java.util.ArrayList<>();

        if (jobName != null && !jobName.isEmpty()) {
            predicates.add(cb.like(root.get("jobName"), "%" + jobName + "%"));
        }
        if (jobGroup != null && !jobGroup.isEmpty()) {
            predicates.add(cb.equal(root.get("jobGroup"), jobGroup));
        }
        if (status != null && !status.isEmpty()) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (startTime != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), startTime));
        }
        if (endTime != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), endTime));
        }

        cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(cb.desc(root.get("startTime")));
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Find job logs by condition with pagination
     */
    public Page<SysJobLog> findByConditionPaginated(String jobName, String jobGroup, String status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // Count query
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<SysJobLog> countRoot = countCq.from(SysJobLog.class);
        List<Predicate> countPredicates = new java.util.ArrayList<>();
        
        if (jobName != null && !jobName.isEmpty()) {
            countPredicates.add(cb.like(countRoot.get("jobName"), "%" + jobName + "%"));
        }
        if (jobGroup != null && !jobGroup.isEmpty()) {
            countPredicates.add(cb.equal(countRoot.get("jobGroup"), jobGroup));
        }
        if (status != null && !status.isEmpty()) {
            countPredicates.add(cb.equal(countRoot.get("status"), status));
        }
        if (startTime != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("startTime"), startTime));
        }
        if (endTime != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("endTime"), endTime));
        }
        
        countCq.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countCq).getSingleResult();
        
        // Data query
        CriteriaQuery<SysJobLog> cq = cb.createQuery(SysJobLog.class);
        Root<SysJobLog> root = cq.from(SysJobLog.class);
        List<Predicate> predicates = new java.util.ArrayList<>();
        
        if (jobName != null && !jobName.isEmpty()) {
            predicates.add(cb.like(root.get("jobName"), "%" + jobName + "%"));
        }
        if (jobGroup != null && !jobGroup.isEmpty()) {
            predicates.add(cb.equal(root.get("jobGroup"), jobGroup));
        }
        if (status != null && !status.isEmpty()) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (startTime != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), startTime));
        }
        if (endTime != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), endTime));
        }
        
        cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(cb.desc(root.get("startTime")));
        TypedQuery<SysJobLog> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    /**
     * Find job logs by job ID
     */
    public List<SysJobLog> findByJobId(Long jobId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SysJobLog> cq = cb.createQuery(SysJobLog.class);
        Root<SysJobLog> root = cq.from(SysJobLog.class);
        cq.select(root).where(cb.equal(root.get("jobId"), jobId)).orderBy(cb.desc(root.get("startTime")));
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Find job log by ID
     */
    public Optional<SysJobLog> findById(Long jobLogId) {
        SysJobLog log = entityManager.find(SysJobLog.class, jobLogId);
        return Optional.ofNullable(log);
    }

    /**
     * Save job log
     */
    @Transactional
    public SysJobLog save(SysJobLog log) {
        if (log.getJobLogId() == null) {
            log.setCreateTime(LocalDateTime.now());
            entityManager.persist(log);
        } else {
            entityManager.merge(log);
        }
        return log;
    }

    /**
     * Delete job log by ID
     */
    @Transactional
    public void deleteById(Long jobLogId) {
        SysJobLog log = entityManager.find(SysJobLog.class, jobLogId);
        if (log != null) {
            entityManager.remove(log);
        }
    }

    /**
     * Delete job logs by IDs
     */
    @Transactional
    public int deleteByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            SysJobLog log = entityManager.find(SysJobLog.class, id);
            if (log != null) {
                entityManager.remove(log);
                count++;
            }
        }
        return count;
    }

    /**
     * Find recent failed logs
     */
    public List<SysJobLog> findRecentFailed(int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SysJobLog> cq = cb.createQuery(SysJobLog.class);
        Root<SysJobLog> root = cq.from(SysJobLog.class);
        cq.select(root)
          .where(cb.equal(root.get("status"), "1"))
          .orderBy(cb.desc(root.get("startTime")));
        TypedQuery<SysJobLog> query = entityManager.createQuery(cq);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    /**
     * Get statistics for dashboard
     */
    public JobLogStatistics getStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // Total executions
        CriteriaQuery<Long> totalCq = cb.createQuery(Long.class);
        Root<SysJobLog> totalRoot = totalCq.from(SysJobLog.class);
        totalCq.select(cb.count(totalRoot));
        List<Predicate> datePredicates = new java.util.ArrayList<>();
        if (startDate != null) {
            datePredicates.add(cb.greaterThanOrEqualTo(totalRoot.get("startTime"), startDate));
        }
        if (endDate != null) {
            datePredicates.add(cb.lessThanOrEqualTo(totalRoot.get("startTime"), endDate));
        }
        totalCq.where(datePredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(totalCq).getSingleResult();

        // Successful executions
        CriteriaQuery<Long> successCq = cb.createQuery(Long.class);
        Root<SysJobLog> successRoot = successCq.from(SysJobLog.class);
        successCq.select(cb.count(successRoot));
        List<Predicate> successPredicates = new java.util.ArrayList<>(datePredicates);
        successPredicates.add(cb.equal(successRoot.get("status"), "0"));
        successCq.where(successPredicates.toArray(new Predicate[0]));
        Long success = entityManager.createQuery(successCq).getSingleResult();

        // Failed executions
        CriteriaQuery<Long> failedCq = cb.createQuery(Long.class);
        Root<SysJobLog> failedRoot = failedCq.from(SysJobLog.class);
        failedCq.select(cb.count(failedRoot));
        List<Predicate> failedPredicates = new java.util.ArrayList<>(datePredicates);
        failedPredicates.add(cb.equal(failedRoot.get("status"), "1"));
        failedCq.where(failedPredicates.toArray(new Predicate[0]));
        Long failed = entityManager.createQuery(failedCq).getSingleResult();

        // Average execution duration
        CriteriaQuery<Double> avgCq = cb.createQuery(Double.class);
        Root<SysJobLog> avgRoot = avgCq.from(SysJobLog.class);
        avgCq.select(cb.avg(avgRoot.get("executionDuration")));
        avgCq.where(datePredicates.toArray(new Predicate[0]));
        Double avgDuration = entityManager.createQuery(avgCq).getSingleResult();

        return new JobLogStatistics(total, success, failed, avgDuration != null ? avgDuration : 0.0);
    }

    /**
     * Inner class for statistics
     */
    public static class JobLogStatistics {
        private final Long totalExecutions;
        private final Long successfulExecutions;
        private final Long failedExecutions;
        private final Double avgExecutionDuration;

        public JobLogStatistics(Long totalExecutions, Long successfulExecutions, Long failedExecutions, Double avgExecutionDuration) {
            this.totalExecutions = totalExecutions;
            this.successfulExecutions = successfulExecutions;
            this.failedExecutions = failedExecutions;
            this.avgExecutionDuration = avgExecutionDuration;
        }

        public Long getTotalExecutions() { return totalExecutions; }
        public Long getSuccessfulExecutions() { return successfulExecutions; }
        public Long getFailedExecutions() { return failedExecutions; }
        public Double getAvgExecutionDuration() { return avgExecutionDuration; }
        public double getSuccessRate() {
            return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100 : 0.0;
        }
    }
}
