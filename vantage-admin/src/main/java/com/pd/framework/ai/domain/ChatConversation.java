package com.pd.framework.ai.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AI Chat Conversation Entity
 * Tracks conversation sessions per user
 */
@Entity
@Table(name = "ai_chat_conversation")
@Data
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long id;

    @Column(name = "username", length = 64, nullable = false)
    private String username;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "status", length = 1)
    private String status = "1"; // 1=active, 0=archived

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @Column(name = "message_count")
    private Integer messageCount = 0;

    public ChatConversation() {}

    public ChatConversation(String username, String title) {
        this.username = username;
        this.title = title;
    }
}
