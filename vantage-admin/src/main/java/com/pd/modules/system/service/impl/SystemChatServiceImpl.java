package com.pd.modules.system.service.impl;

import com.pd.framework.ai.service.AiChatService;
import com.pd.framework.ai.service.KnowledgeBaseService;
import com.pd.framework.ai.service.ToolExecutionService;
import com.pd.modules.system.api.SystemChatService;
import com.pd.modules.system.security.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class SystemChatServiceImpl implements SystemChatService {

    private static final Logger log = LoggerFactory.getLogger(SystemChatServiceImpl.class);

    private final AiChatService aiChatService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final ToolExecutionService toolExecutionService;

    public SystemChatServiceImpl(
            @org.springframework.beans.factory.annotation.Autowired(required = false) AiChatService aiChatService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) KnowledgeBaseService knowledgeBaseService,
            ToolExecutionService toolExecutionService) {
        this.aiChatService = aiChatService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.toolExecutionService = toolExecutionService;
    }

    @Override
    public Map<String, Object> chat(String message, List<Map<String, Object>> toolResults, String username) {
        if (toolResults != null && !toolResults.isEmpty()) {
            return buildToolResponse(toolResults);
        }

        if (aiChatService != null && aiChatService.isEnabled()) {
            try {
                String aiResponse = aiChatService.chat(message, username);
                List<Map<String, Object>> toolCalls = extractToolCalls(aiResponse, message);

                Map<String, Object> result = new HashMap<>();
                if (!toolCalls.isEmpty()) {
                    List<Map<String, Object>> executedResults = executeToolCalls(toolCalls);
                    result.put("toolCalls", toolCalls);
                    result.put("toolResults", executedResults);
                    result.put("response", null);

                    // Recursively call with tool results to get AI summary
                    String summary = aiChatService.chat(
                            "Based on these tool execution results, provide a summary: " + formatToolResults(executedResults),
                            username);
                    result.put("summary", summary);
                } else {
                    result.put("response", aiResponse);
                    result.put("toolCalls", null);
                }
                return result;
            } catch (Exception e) {
                return processMessageFallback(message, null);
            }
        }
        return processMessageFallback(message, null);
    }

    @Override
    public Map<String, Object> chatStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", aiChatService != null && aiChatService.isEnabled());
        if (knowledgeBaseService != null) {
            var stats = knowledgeBaseService.getStats();
            status.put("knowledgeCount", stats.total());
        } else {
            status.put("knowledgeCount", 0);
        }
        return status;
    }

    @Override
    public Map<String, Object> refreshKnowledge() {
        if (knowledgeBaseService != null && aiChatService != null) {
            knowledgeBaseService.initializeKnowledgeBase();
            return Map.of("message", "Knowledge base refreshed");
        }
        return Map.of("error", "Knowledge service not available");
    }

    @Override
    public Map<String, Object> knowledgeStats() {
        if (knowledgeBaseService != null) {
            var stats = knowledgeBaseService.getStats();
            Map<String, Object> map = new HashMap<>();
            map.put("total", stats.total());
            map.put("userManagement", stats.userManagement());
            map.put("roleManagement", stats.roleManagement());
            map.put("jobScheduling", stats.jobScheduling());
            return map;
        }
        return Collections.emptyMap();
    }

    @Override
    public SseEmitter chatStream(String message, String username) {
        SseEmitter emitter = new SseEmitter(120_000L);
        if (aiChatService == null || !aiChatService.isEnabled()) {
            try {
                emitter.send(SseEmitter.event().data("AI service is not available. Please ensure Ollama is running."));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }
        CompletableFuture.runAsync(() -> {
            try {
                aiChatService.chatStream(message, username)
                        .doOnNext(token -> {
                            try { emitter.send(SseEmitter.event().data(token)); }
                            catch (IOException e) { log.warn("Failed to send SSE token", e); }
                        })
                        .doOnComplete(() -> emitter.complete())
                        .doOnError(throwable -> {
                            try { emitter.send(SseEmitter.event().data("Error: " + throwable.getMessage())); }
                            catch (IOException e) { /* ignore */ }
                            emitter.completeWithError(throwable);
                        })
                        .subscribe();
            } catch (Exception e) {
                log.error("Chat stream error", e);
                try { emitter.send(SseEmitter.event().data("Internal error occurred")); }
                catch (IOException ex) { /* ignore */ }
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @Override
    public List<Map<String, Object>> getConversationHistory(String username) {
        if (aiChatService != null) {
            return aiChatService.getUserConversationHistory(username);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getConversationsList(String username) {
        if (aiChatService != null) {
            return aiChatService.getConversationsList(username);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getConversationHistoryById(Long conversationId) {
        if (aiChatService != null) {
            return aiChatService.getConversationHistory(conversationId);
        }
        return Collections.emptyList();
    }

    @Override
    public String clearMemory(String username) {
        if (aiChatService != null) {
            aiChatService.clearMemory(username);
            return "Conversation cleared";
        }
        throw new RuntimeException("AI service not available");
    }

    @Override
    public String deleteConversation(Long conversationId, String username) {
        if (aiChatService != null) {
            aiChatService.deleteConversation(conversationId);
            return "Conversation deleted";
        }
        throw new RuntimeException("AI service not available");
    }

    @Override
    public String deleteAllConversations(String username) {
        if (aiChatService != null) {
            aiChatService.deleteAllConversations(username);
            return "All conversations deleted";
        }
        throw new RuntimeException("AI service not available");
    }

    private boolean matchesAny(String msg, String... patterns) {
        return Arrays.stream(patterns).anyMatch(msg::contains);
    }

    private List<Map<String, Object>> executeToolCalls(List<Map<String, Object>> toolCalls) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> toolCall : toolCalls) {
            String toolName = (String) toolCall.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) toolCall.getOrDefault("arguments", Collections.emptyMap());
            results.add(toolExecutionService.executeTool(toolName, arguments));
        }
        return results;
    }

    private String formatToolResults(List<Map<String, Object>> toolResults) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> result : toolResults) {
            String name = (String) result.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("result");
            sb.append("Tool: ").append(name).append("\n");
            if (data != null) {
                sb.append("Success: ").append(data.get("success")).append("\n");
                if (data.containsKey("message")) {
                    sb.append("Message: ").append(data.get("message")).append("\n");
                }
                if (data.containsKey("count")) {
                    sb.append("Count: ").append(data.get("count")).append("\n");
                }
            }
            sb.append("---\n");
        }
        return sb.toString();
    }

    private Map<String, Object> processMessageFallback(String message, List<Map<String, Object>> toolResults) {
        if (toolResults != null && !toolResults.isEmpty()) {
            return buildToolResponse(toolResults);
        }
        List<Map<String, Object>> toolCalls = extractToolCalls(null, message);
        if (!toolCalls.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("toolCalls", toolCalls);
            result.put("response", null);
            return result;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("response", getGeneralResponse(message));
        result.put("toolCalls", null);
        return result;
    }

    private String getGeneralResponse(String message) {
        String lower = message.toLowerCase();
        if (matchesAny(lower, "hello", "hi", "hey")) {
            return "Hello! I'm your Vantage Admin assistant. I can help you with system management, job scheduling, reports & monitoring, and code generation.";
        }
        if (lower.contains("help")) {
            return "I can help with users, roles, jobs, reports, logs, monitoring, and code generation. Try saying \"help\" for a full list of commands!";
        }
        if (lower.contains("thank")) {
            return "You're welcome! Is there anything else I can help you with?";
        }
        return "I understand you're asking about: \"" + message + "\"\n\nI can help with users, roles, jobs, reports, logs, monitoring, and code generation.";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildToolResponse(List<Map<String, Object>> toolResults) {
        Map<String, Object> result = new HashMap<>();
        StringBuilder response = new StringBuilder("I've completed the following actions:\n\n");
        for (Map<String, Object> toolResult : toolResults) {
            String toolName = (String) toolResult.get("name");
            Map<String, Object> toolData = (Map<String, Object>) toolResult.get("result");
            boolean success = toolData != null && Boolean.TRUE.equals(toolData.get("success"));
            if (success) {
                response.append("\u2705 ").append(formatToolName(toolName)).append(" - Success\n");
            } else {
                response.append("\u274c ").append(formatToolName(toolName)).append(" - Failed\n");
            }
        }
        response.append("\nIs there anything else you'd like me to help with?");
        result.put("response", response.toString());
        return result;
    }

    private String formatToolName(String name) {
        return name.replaceAll("(\\p{Lu})", " $1").trim();
    }

    private List<Map<String, Object>> extractToolCalls(String aiResponse, String originalMessage) {
        List<Map<String, Object>> toolCalls = new ArrayList<>();
        String lower = originalMessage.toLowerCase();

        if (matchesAny(lower, "create user", "add user", "new user")) {
            Map<String, Object> args = new HashMap<>();
            String name = extractName(originalMessage);
            if (name != null) {
                String[] parts = name.split("\\s+", 2);
                args.put("loginName", parts[0].toLowerCase().replace(" ", "."));
                args.put("userName", name);
            }
            toolCalls.add(Map.of("name", "createUser", "arguments", args));
        } else if (matchesAny(lower, "list user", "show user", "all user", "get user")) {
            toolCalls.add(Map.of("name", "listUsers", "arguments", Collections.emptyMap()));
        } else if (matchesAny(lower, "delete user", "remove user")) {
            Map<String, Object> args = new HashMap<>();
            Long userId = extractUserId(originalMessage);
            if (userId != null) args.put("userId", userId);
            toolCalls.add(Map.of("name", "deleteUser", "arguments", args));
        } else if (matchesAny(lower, "list role", "show role", "all role", "get role")) {
            toolCalls.add(Map.of("name", "listRoles", "arguments", Collections.emptyMap()));
        } else if (matchesAny(lower, "create role", "add role", "new role")) {
            Map<String, Object> args = new HashMap<>();
            String name = extractName(originalMessage);
            if (name != null) {
                args.put("roleName", name);
                args.put("roleKey", name.toLowerCase().replace(" ", "_"));
            }
            toolCalls.add(Map.of("name", "createRole", "arguments", args));
        } else if (matchesAny(lower, "run job", "execute job", "trigger job")) {
            Map<String, Object> args = new HashMap<>();
            Long jobId = extractJobId(originalMessage);
            if (jobId != null) args.put("jobId", jobId);
            String jobName = extractName(originalMessage);
            if (jobName != null) args.put("jobName", jobName);
            toolCalls.add(Map.of("name", "executeJob", "arguments", args));
        } else if (matchesAny(lower, "list job", "show job", "all job", "get job")) {
            toolCalls.add(Map.of("name", "listJobs", "arguments", Collections.emptyMap()));
        } else if (matchesAny(lower, "show report", "get report", "pull report")) {
            toolCalls.add(Map.of("name", "generateReport", "arguments", Collections.emptyMap()));
        } else if (matchesAny(lower, "operation log", "oper log", "audit log")) {
            toolCalls.add(Map.of("name", "getOperationLogs", "arguments", Collections.emptyMap()));
        } else if (matchesAny(lower, "config", "configuration", "setting")) {
            Map<String, Object> args = new HashMap<>();
            if (lower.contains("key")) {
                args.put("configKey", extractConfigKey(originalMessage));
            }
            toolCalls.add(Map.of("name", "getConfig", "arguments", args));
        }

        return toolCalls;
    }

    private String extractName(String message) {
        String[] patterns = {"name ", "named ", "call "};
        for (String pattern : patterns) {
            int idx = message.toLowerCase().indexOf(pattern);
            if (idx != -1) {
                String after = message.substring(idx + pattern.length()).trim();
                int end = after.indexOf(" ");
                if (end == -1) end = after.length();
                if (end > 0) return after.substring(0, Math.min(end + 20, after.length())).replaceAll("[\"']", "").trim();
            }
        }
        return null;
    }

    private Long extractUserId(String message) {
        try {
            String[] words = message.split("\\s+");
            for (String word : words) {
                String cleaned = word.replaceAll("[^0-9]", "");
                if (!cleaned.isEmpty()) return Long.parseLong(cleaned);
            }
        } catch (NumberFormatException ignored) {}
        return null;
    }

    private Long extractJobId(String message) {
        return extractUserId(message);
    }

    private String extractConfigKey(String message) {
        int idx = message.toLowerCase().indexOf("key");
        if (idx != -1) {
            return message.substring(idx + 3).trim().replaceAll("[\"']", "");
        }
        return null;
    }
}
