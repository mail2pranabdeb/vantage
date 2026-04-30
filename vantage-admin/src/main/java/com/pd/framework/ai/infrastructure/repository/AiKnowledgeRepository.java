package com.pd.framework.ai.infrastructure.repository;

import com.pd.framework.ai.domain.AiKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI Knowledge Repository
 */
@Repository
public interface AiKnowledgeRepository extends JpaRepository<AiKnowledge, Long> {

    /**
     * Find active knowledge by category
     */
    List<AiKnowledge> findByCategoryAndStatusOrderByCreateTimeDesc(String category, String status);

    /**
     * Find all active knowledge
     */
    List<AiKnowledge> findByStatusOrderByCreateTimeDesc(String status);

    /**
     * Search by title or keywords
     */
    @Query("SELECT k FROM AiKnowledge k WHERE k.status = '1' AND " +
           "(LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<AiKnowledge> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Count by category
     */
    long countByCategoryAndStatus(String category, String status);
}
