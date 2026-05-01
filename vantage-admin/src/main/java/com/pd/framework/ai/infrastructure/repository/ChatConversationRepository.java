package com.pd.framework.ai.infrastructure.repository;

import com.pd.framework.ai.domain.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI Chat Conversation Repository
 */
@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    List<ChatConversation> findByUsernameAndStatusOrderByUpdateTimeDesc(String username, String status);

    Optional<ChatConversation> findFirstByUsernameAndStatusOrderByCreateTimeDesc(String username, String status);

    long countByUsernameAndStatus(String username, String status);

    void deleteByUsername(String username);
}
