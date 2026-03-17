package com.pd.framework.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI Configuration Properties
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * Enable AI features (default: false)
     * Set to true when Ollama is running
     */
    private boolean enabled = false;

    /**
     * Ollama base URL
     */
    private String ollamaBaseUrl = "http://localhost:11434";

    /**
     * LLM model name (e.g., llama3, mistral, phi3)
     */
    private String chatModel = "llama3";

    /**
     * Temperature for response generation (0.0 - 1.0)
     */
    private double temperature = 0.7;

    /**
     * Maximum tokens for response
     */
    private int maxTokens = 1024;

    /**
     * Enable RAG (Retrieval-Augmented Generation)
     */
    private boolean ragEnabled = true;

    /**
     * System prompt for AI assistant
     */
    private String systemPrompt = """
            You are the Vantage Admin AI Assistant, helping users manage the Vantage Admin Platform.
            
            Vantage Admin is a business management system with these key features:
            - User Management: Create, update, delete users with roles and permissions
            - Role Management: Define roles with specific permissions
            - Menu Management: Configure navigation menus
            - Config Management: System configuration settings
            - Dict Management: Dictionary/data dictionary management
            - Post Management: Job position management
            - Notice Management: System announcements
            - Job Scheduling: Quartz-based scheduled tasks
            - Code Generation: Generate CRUD code from database tables
            - Operation Logging: Automatic logging of all operations
            - Login Monitoring: Track login attempts and security
            
            When users ask about system features, provide helpful, accurate information.
            For operational tasks (create user, delete role, etc.), use the available tools.
            Always be concise and professional.
            
            If you're unsure about something, admit it and suggest consulting the documentation.
            """;

    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getOllamaBaseUrl() { return ollamaBaseUrl; }
    public void setOllamaBaseUrl(String ollamaBaseUrl) { this.ollamaBaseUrl = ollamaBaseUrl; }
    public String getChatModel() { return chatModel; }
    public void setChatModel(String chatModel) { this.chatModel = chatModel; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public boolean isRagEnabled() { return ragEnabled; }
    public void setRagEnabled(boolean ragEnabled) { this.ragEnabled = ragEnabled; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
}
