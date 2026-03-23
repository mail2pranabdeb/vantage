package com.pd.modules.quartz.infrastructure.repository;

import com.pd.modules.quartz.domain.EmailTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class EmailTemplateRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find all templates
     */
    public List<EmailTemplate> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<EmailTemplate> cq = cb.createQuery(EmailTemplate.class);
        Root<EmailTemplate> root = cq.from(EmailTemplate.class);
        cq.select(root).orderBy(cb.desc(root.get("createTime")));
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Find active templates
     */
    public List<EmailTemplate> findActive() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<EmailTemplate> cq = cb.createQuery(EmailTemplate.class);
        Root<EmailTemplate> root = cq.from(EmailTemplate.class);
        cq.select(root).where(cb.equal(root.get("isActive"), true));
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Find template by type
     */
    public Optional<EmailTemplate> findByType(String templateType) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<EmailTemplate> cq = cb.createQuery(EmailTemplate.class);
        Root<EmailTemplate> root = cq.from(EmailTemplate.class);
        cq.select(root).where(
            cb.equal(root.get("templateType"), templateType),
            cb.equal(root.get("isActive"), true)
        );
        List<EmailTemplate> results = entityManager.createQuery(cq).getResultList();
        return results.stream().findFirst();
    }

    /**
     * Find default template by type
     */
    public Optional<EmailTemplate> findDefaultByType(String templateType) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<EmailTemplate> cq = cb.createQuery(EmailTemplate.class);
        Root<EmailTemplate> root = cq.from(EmailTemplate.class);
        cq.select(root).where(
            cb.equal(root.get("templateType"), templateType),
            cb.equal(root.get("isDefault"), true),
            cb.equal(root.get("isActive"), true)
        );
        List<EmailTemplate> results = entityManager.createQuery(cq).getResultList();
        return results.stream().findFirst();
    }

    /**
     * Find template by ID
     */
    public Optional<EmailTemplate> findById(Long templateId) {
        EmailTemplate template = entityManager.find(EmailTemplate.class, templateId);
        return Optional.ofNullable(template);
    }

    /**
     * Find template by name
     */
    public Optional<EmailTemplate> findByName(String templateName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<EmailTemplate> cq = cb.createQuery(EmailTemplate.class);
        Root<EmailTemplate> root = cq.from(EmailTemplate.class);
        cq.select(root).where(cb.equal(root.get("templateName"), templateName));
        List<EmailTemplate> results = entityManager.createQuery(cq).getResultList();
        return results.stream().findFirst();
    }

    /**
     * Save template
     */
    @Transactional
    public EmailTemplate save(EmailTemplate template) {
        if (template.getTemplateId() == null) {
            template.setCreateTime(LocalDateTime.now());
            entityManager.persist(template);
        } else {
            template.setUpdateTime(LocalDateTime.now());
            entityManager.merge(template);
        }
        return template;
    }

    /**
     * Delete template
     */
    @Transactional
    public void deleteById(Long templateId) {
        EmailTemplate template = entityManager.find(EmailTemplate.class, templateId);
        if (template != null) {
            entityManager.remove(template);
        }
    }

    /**
     * Set template as default
     */
    @Transactional
    public void setAsDefault(Long templateId, String templateType) {
        // Unset all other defaults for this type
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<EmailTemplate> update = cb.createCriteriaUpdate(EmailTemplate.class);
        Root<EmailTemplate> root = update.from(EmailTemplate.class);
        update.set("isDefault", false)
              .where(cb.equal(root.get("templateType"), templateType));
        entityManager.createQuery(update).executeUpdate();

        // Set new default
        EmailTemplate template = entityManager.find(EmailTemplate.class, templateId);
        if (template != null) {
            template.setIsDefault(true);
            entityManager.merge(template);
        }
    }
}
