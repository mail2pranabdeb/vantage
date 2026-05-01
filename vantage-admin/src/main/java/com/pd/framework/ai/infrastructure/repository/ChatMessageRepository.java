package com.pd.framework.ai.infrastructure.repository;

import com.pd.framework.ai.domain.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI Chat Message Repository
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    List<AiChatMessage> findByConversationIdOrderByCreateTimeAsc(Long conversationId);

    List<AiChatMessage> findByConversationIdAndRoleOrderByCreateTimeAsc(Long conversationId, String role);

    void deleteByConversationId(Long conversationId);

    long countByConversationId(Long conversationId);

    void deleteByUsername(String username);
}
