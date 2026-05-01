package com.pd.framework.ai.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AI Chat Message Entity
 * Stores individual messages within conversations
 */
@Entity
@Table(name = "ai_chat_message")
@Data
public class AiChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "username", length = 64, nullable = false)
    private String username;

    @Column(name = "role", length = 20, nullable = false)
    private String role; // user, assistant, system

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "tokens")
    private Integer tokens;

    @Column(name = "tool_calls", columnDefinition = "TEXT")
    private String toolCalls;

    @Column(name = "tool_results", columnDefinition = "TEXT")
    private String toolResults;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    public AiChatMessage() {}

    public AiChatMessage(Long conversationId, String username, String role, String content) {
        this.conversationId = conversationId;
        this.username = username;
        this.role = role;
        this.content = content;
    }
}
