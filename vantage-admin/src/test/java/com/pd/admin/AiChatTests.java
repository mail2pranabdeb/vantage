package com.pd.admin;

import com.pd.framework.ai.domain.AiChatMessage;
import com.pd.framework.ai.domain.ChatConversation;
import com.pd.framework.ai.infrastructure.repository.ChatConversationRepository;
import com.pd.framework.ai.infrastructure.repository.ChatMessageRepository;
import com.pd.framework.ai.service.AiChatService;
import com.pd.framework.ai.service.ToolExecutionService;
import com.pd.modules.system.api.SystemChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AI Chat integration tests.
 * Verifies conversation persistence, tool execution, and chat service.
 */
@SpringBootTest
class AiChatTests {

    @Autowired
    private ChatConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired(required = false)
    private AiChatService aiChatService;

    @Autowired
    private SystemChatService systemChatService;

    @Autowired
    private ToolExecutionService toolExecutionService;

    private String uniqueUser(String suffix) {
        return "testuser_" + UUID.randomUUID().toString().substring(0, 8) + "_" + suffix;
    }

    @Test
    @DisplayName("AiChatService bean exists")
    void aiChatServiceExists() {
        assertThat(aiChatService).isNotNull();
    }

    @Test
    @DisplayName("SystemChatService bean exists")
    void systemChatServiceExists() {
        assertThat(systemChatService).isNotNull();
    }

    @Test
    @DisplayName("ToolExecutionService bean exists")
    void toolExecutionServiceExists() {
        assertThat(toolExecutionService).isNotNull();
    }

    @Test
    @DisplayName("ChatConversationRepository works")
    void conversationRepositoryWorks() {
        String user = uniqueUser("conv");
        ChatConversation conv = new ChatConversation(user, "Test Conversation");
        conv = conversationRepository.save(conv);

        assertThat(conv.getId()).isNotNull();
        assertThat(conversationRepository.findById(conv.getId())).isPresent();

        List<ChatConversation> userConvs = conversationRepository.findByUsernameAndStatusOrderByUpdateTimeDesc(user, "1");
        assertThat(userConvs).isNotEmpty();
        assertThat(userConvs.get(0).getId()).isEqualTo(conv.getId());

        conversationRepository.deleteById(conv.getId());
    }

    @Test
    @DisplayName("ChatMessageRepository works")
    @Transactional
    void messageRepositoryWorks() {
        String user = uniqueUser("msg");
        ChatConversation conv = new ChatConversation(user, "Test Conversation 2");
        conv = conversationRepository.save(conv);

        AiChatMessage userMsg = new AiChatMessage(conv.getId(), user, "user", "Hello!");
        userMsg = messageRepository.save(userMsg);

        assertThat(userMsg.getId()).isNotNull();
        assertThat(messageRepository.findByConversationIdOrderByCreateTimeAsc(conv.getId())).hasSize(1);

        messageRepository.deleteByConversationId(conv.getId());
        conversationRepository.deleteById(conv.getId());
    }

    @Test
    @DisplayName("Fallback chat response works when AI disabled")
    void fallbackChatResponse() {
        String user = uniqueUser("chat");
        Map<String, Object> result = systemChatService.chat("hello", null, user);
        assertThat(result).containsKey("response");
        assertThat(result).containsKey("toolCalls");
    }

    @Test
    @DisplayName("Tool execution returns result for listUsers")
    void toolExecutionListUsers() {
        Map<String, Object> result = toolExecutionService.executeTool("listUsers", Map.of());
        assertThat(result).containsKey("result");
        Map<String, Object> inner = (Map<String, Object>) result.get("result");
        assertThat(inner.get("success")).isEqualTo(true);
        assertThat(inner).containsKey("count");
    }

    @Test
    @DisplayName("Tool execution returns result for listRoles")
    void toolExecutionListRoles() {
        Map<String, Object> result = toolExecutionService.executeTool("listRoles", Map.of());
        assertThat(result).containsKey("result");
        Map<String, Object> inner = (Map<String, Object>) result.get("result");
        assertThat(inner.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("Tool execution handles unknown tool gracefully")
    void toolExecutionUnknownTool() {
        Map<String, Object> result = toolExecutionService.executeTool("nonExistentTool", Map.of());
        assertThat(result.get("name")).isEqualTo("nonExistentTool");
        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("message")).asString().contains("Unknown tool");
    }

    @Test
    @DisplayName("Conversation history returns empty for new user")
    void conversationHistoryEmptyForNewUser() {
        List<Map<String, Object>> history = systemChatService.getConversationHistory("nonexistent_user_12345");
        assertThat(history).isEmpty();
    }

    @Test
    @DisplayName("Conversations list returns empty for new user")
    void conversationsListEmptyForNewUser() {
        List<Map<String, Object>> list = systemChatService.getConversationsList("nonexistent_user_12345");
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("Clear memory works for new user")
    void clearMemoryWorks() {
        String result = systemChatService.clearMemory(uniqueUser("clear"));
        assertThat(result).isEqualTo("Conversation cleared");
    }

    @Test
    @DisplayName("Delete all conversations works for user")
    void deleteAllConversationsWorks() {
        String result = systemChatService.deleteAllConversations(uniqueUser("deleteall"));
        assertThat(result).isEqualTo("All conversations deleted");
    }

    @Test
    @DisplayName("Chat status returns enabled flag")
    void chatStatusReturnsEnabled() {
        Map<String, Object> status = systemChatService.chatStatus();
        assertThat(status).containsKey("enabled");
        assertThat(status).containsKey("knowledgeCount");
    }

    @Test
    @DisplayName("Knowledge stats returns structure")
    void knowledgeStatsReturnsStructure() {
        Map<String, Object> stats = systemChatService.knowledgeStats();
        assertThat(stats).containsKey("total");
    }
}
