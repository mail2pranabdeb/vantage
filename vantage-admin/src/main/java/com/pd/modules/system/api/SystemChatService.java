package com.pd.modules.system.api;

import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * System module public API for AI chat operations.
 */
public interface SystemChatService {

    Map<String, Object> chat(String message, List<Map<String, Object>> toolResults, String username);

    Map<String, Object> chatStatus();

    Map<String, Object> refreshKnowledge();

    Map<String, Object> knowledgeStats();

    SseEmitter chatStream(String message, String username);

    List<Map<String, Object>> getConversationHistory(String username);

    List<Map<String, Object>> getConversationsList(String username);

    List<Map<String, Object>> getConversationHistoryById(Long conversationId);

    String clearMemory(String username);

    String deleteConversation(Long conversationId, String username);

    String deleteAllConversations(String username);
}
