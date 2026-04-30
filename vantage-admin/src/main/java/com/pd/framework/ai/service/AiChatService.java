package com.pd.framework.ai.service;

import com.pd.framework.ai.config.AiProperties;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Chat Service with RAG (Retrieval-Augmented Generation)
 * Provides intelligent responses using local LLM and knowledge base
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private final AiProperties aiProperties;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ContentRetriever contentRetriever;
    private final Map<String, ChatMemory> userMemories = new ConcurrentHashMap<>();
    private final ChatMemory defaultMemory;
    private ChatLanguageModel chatModel;
    private StreamingChatLanguageModel streamingChatModel;

    public AiChatService(
            AiProperties aiProperties,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            ContentRetriever contentRetriever,
            ChatMemory defaultMemory
    ) {
        this.aiProperties = aiProperties;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.contentRetriever = contentRetriever;
        this.defaultMemory = defaultMemory;

        if (aiProperties.isEnabled()) {
            this.chatModel = buildChatModel(aiProperties);
            this.streamingChatModel = buildStreamingChatModel(aiProperties);
        }
    }

    private ChatLanguageModel buildChatModel(AiProperties props) {
        return OllamaChatModel.builder()
                .baseUrl(props.getOllamaBaseUrl())
                .modelName(props.getChatModel())
                .temperature(props.getTemperature())
                .maxRetries(1)
                .timeout(java.time.Duration.ofSeconds(props.getTimeout()))
                .build();
    }

    private StreamingChatLanguageModel buildStreamingChatModel(AiProperties props) {
        return OllamaStreamingChatModel.builder()
                .baseUrl(props.getOllamaBaseUrl())
                .modelName(props.getChatModel())
                .temperature(props.getTemperature())
                .timeout(java.time.Duration.ofSeconds(props.getTimeout()))
                .build();
    }

    /**
     * Get per-user chat memory (creates new one if not exists)
     */
    private ChatMemory getUserMemory(String username) {
        if (username == null || username.isEmpty()) {
            return defaultMemory;
        }
        return userMemories.computeIfAbsent(username, k -> {
            // Create a new memory window for this user
            return dev.langchain4j.memory.chat.MessageWindowChatMemory.withMaxMessages(20);
        });
    }

    /**
     * Process user message and generate AI response
     */
    public String chat(String userMessage, String username) {
        if (!aiProperties.isEnabled()) {
            return getFallbackResponse(userMessage);
        }

        ChatMemory memory = getUserMemory(username);
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new SystemMessage(aiProperties.getSystemPrompt()));
            messages.addAll(memory.messages());
            String contextualMessage = buildContextualMessage(userMessage, username);
            messages.add(new UserMessage(contextualMessage));

            AiMessage aiMessage = chatModel.generate(messages).content();
            String response = aiMessage.text();

            memory.add(new UserMessage(userMessage));
            memory.add(new AiMessage(response));

            return response;
        } catch (Exception e) {
            log.error("AI chat error", e);
            return getFallbackResponse(userMessage);
        }
    }

    /**
     * Streaming response using Flux (SSE-compatible)
     */
    public Flux<String> chatStream(String userMessage, String username) {
        if (!aiProperties.isEnabled()) {
            return Flux.just(getFallbackResponse(userMessage));
        }

        if (streamingChatModel == null) {
            // Fallback to non-streaming
            return Flux.just(chat(userMessage, username));
        }

        ChatMemory memory = getUserMemory(username);
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new SystemMessage(aiProperties.getSystemPrompt()));
            messages.addAll(memory.messages());
            String contextualMessage = buildContextualMessage(userMessage, username);
            messages.add(new UserMessage(contextualMessage));

            // Build a string builder to collect the full response
            StringBuilder fullResponse = new StringBuilder();

            return Flux.create(sink -> {
                try {
                    streamingChatModel.generate(messages, new dev.langchain4j.model.StreamingResponseHandler<AiMessage>() {
                        @Override
                        public void onNext(String token) {
                            fullResponse.append(token);
                            sink.next(token);
                        }

                        @Override
                        public void onComplete(dev.langchain4j.model.output.Response<AiMessage> response) {
                            String responseText = fullResponse.toString();
                            memory.add(new UserMessage(userMessage));
                            memory.add(new AiMessage(responseText));
                            sink.complete();
                        }

                        @Override
                        public void onError(Throwable error) {
                            log.error("Streaming error", error);
                            sink.error(error);
                        }
                    });
                } catch (Exception e) {
                    log.error("Streaming generation failed", e);
                    sink.error(e);
                }
            });
        } catch (Exception e) {
            log.error("AI stream error", e);
            return Flux.just(getFallbackResponse(userMessage));
        }
    }

    /**
     * Build contextual message with RAG-retrieved information
     */
    private String buildContextualMessage(String userMessage, String username) {
        StringBuilder context = new StringBuilder();

        if (aiProperties.isRagEnabled()) {
            List<Content> relevantContents = contentRetriever.retrieve(new Query(userMessage));

            if (!relevantContents.isEmpty()) {
                context.append("Relevant information from knowledge base:\n\n");
                for (int i = 0; i < relevantContents.size(); i++) {
                    context.append("[").append(i + 1).append("] ")
                           .append(relevantContents.get(i).textSegment().text())
                           .append("\n\n");
                }
                context.append("---\n\n");
            }
        }

        context.append("User: ").append(username).append("\n");
        context.append("Question: ").append(userMessage);

        return context.toString();
    }

    /**
     * Add document to knowledge base
     */
    public void addKnowledge(String title, String content, String category) {
        try {
            DocumentSplitter splitter = DocumentSplitters.recursive(300, 30);
            Document document = Document.from(content);
            List<TextSegment> segments = splitter.split(document);

            for (TextSegment segment : segments) {
                segment.metadata().put("title", title);
                segment.metadata().put("category", category);
            }

            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
            embeddingStore.addAll(embeddings, segments);

            log.info("Added knowledge: {} with {} segments", title, segments.size());

        } catch (Exception e) {
            log.error("Error adding knowledge: {}", title, e);
        }
    }

    /**
     * Add multiple documents to knowledge base
     */
    public void addKnowledgeBatch(List<KnowledgeDocument> documents) {
        for (KnowledgeDocument doc : documents) {
            addKnowledge(doc.title(), doc.content(), doc.category());
        }
    }

    /**
     * Clear chat memory for a specific user
     */
    public void clearMemory(String username) {
        if (username != null && !username.isEmpty()) {
            ChatMemory memory = userMemories.remove(username);
            if (memory != null) memory.clear();
        } else {
            defaultMemory.clear();
        }
    }

    /**
     * Get all usernames who have conversation history
     */
    public List<String> getUserConversations() {
        return new ArrayList<>(userMemories.keySet());
    }

    /**
     * Get conversation history for a specific user
     */
    public List<Map<String, Object>> getUserConversationHistory(String username) {
        ChatMemory memory = getUserMemory(username);
        List<Map<String, Object>> history = new ArrayList<>();
        for (ChatMessage msg : memory.messages()) {
            Map<String, Object> entry = new ConcurrentHashMap<>();
            if (msg instanceof UserMessage um) {
                entry.put("role", "user");
                entry.put("content", um.singleText());
            } else if (msg instanceof AiMessage am) {
                entry.put("role", "assistant");
                entry.put("content", am.text());
            }
            history.add(entry);
        }
        return history;
    }

    /**
     * Clear all user memories
     */
    public void clearAllMemories() {
        userMemories.values().forEach(ChatMemory::clear);
        userMemories.clear();
        defaultMemory.clear();
    }

    /**
     * Get fallback response when AI is unavailable
     */
    private String getFallbackResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "Hello! I'm your Vantage Admin assistant. I can help you manage users, roles, and system configurations. What would you like to do today?";
        } else if (lowerMessage.contains("help")) {
            return """
                I can help you with:

                1. **User Management**
                   - "Create a user with name John Doe"
                   - "List all users"
                   - "Delete user 123"

                2. **Role Management**
                   - "Create a role named Admin"
                   - "List all roles"

                3. **System Information**
                   - "Show system config"
                   - "View operation logs"

                Just ask in natural language!
                """;
        } else if (lowerMessage.contains("thank")) {
            return "You're welcome! Is there anything else I can help you with?";
        } else {
            return """
                I understand you're asking about: "%s"

                I can help you create users, manage roles, and configure the system.
                Try asking me to:
                • "Create a user with name John Doe"
                • "List all users"
                • "Create a role named Manager"
                """.formatted(userMessage);
        }
    }

    /**
     * Check if AI is enabled
     */
    public boolean isEnabled() {
        return aiProperties.isEnabled();
    }

    /**
     * Knowledge document for batch loading
     */
    public record KnowledgeDocument(String title, String content, String category) {}
}
