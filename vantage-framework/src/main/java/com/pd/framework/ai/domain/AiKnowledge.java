package com.pd.framework.ai.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AI Knowledge Base Entity
 * Stores documents for RAG (Retrieval-Augmented Generation)
 */
@Entity
@Table(name = "ai_knowledge")
@Data
public class AiKnowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "knowledge_id")
    private Long id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "keywords", length = 500)
    private String keywords;

    @Column(name = "status", length = 1)
    private String status = "1"; // 1=active, 0=inactive

    @Column(name = "create_by", length = 64)
    private String createBy;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_by", length = 64)
    private String updateBy;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "remark", length = 500)
    private String remark;

    public AiKnowledge() {}

    public AiKnowledge(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }
}
