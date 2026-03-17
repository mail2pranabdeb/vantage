package com.pd.framework.ai.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AI Knowledge Base Entity
 * Stores documents for RAG (Retrieval-Augmented Generation)
 */
@Entity
@Table(name = "ai_knowledge")
public class AiKnowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ai_knowledge_seq")
    @SequenceGenerator(name = "ai_knowledge_seq", sequenceName = "ai_knowledge_seq", allocationSize = 1)
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

    // Constructors
    public AiKnowledge() {}

    public AiKnowledge(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
