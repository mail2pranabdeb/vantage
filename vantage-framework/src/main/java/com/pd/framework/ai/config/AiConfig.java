package com.pd.framework.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI Configuration for LangChain4j
 * Provides embedding models, chat memory, and RAG components
 */
@Configuration
public class AiConfig {

    /**
     * Local embedding model (quantized for performance)
     * Runs entirely in-memory, no external service required
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel();
    }

    /**
     * In-memory embedding store for document vectors
     * In production, replace with persistent store (e.g., pgvector, Chroma)
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * Content retriever for RAG (Retrieval-Augmented Generation)
     * Retrieves relevant documents based on query embeddings
     */
    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel
    ) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.6)
                .build();
    }

    /**
     * Chat memory with window of last 10 messages
     * Maintains conversation context for multi-turn conversations
     */
    @Bean
    public MessageWindowChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }
}
