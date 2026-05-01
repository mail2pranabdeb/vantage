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

    String clearMemory(String username);
}
