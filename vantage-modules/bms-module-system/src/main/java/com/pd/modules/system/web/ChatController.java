package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.framework.ai.service.AiChatService;
import com.pd.framework.ai.service.KnowledgeBaseService;
import com.pd.modules.system.security.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Chat controller with AI and MCP tool integration
 * Enhanced with job execution and report pulling capabilities
 */
@RestController
@RequestMapping("/api")
public class ChatController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired(required = false)
    private AiChatService aiChatService;

    @Autowired(required = false)
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * Chat endpoint with AI-powered responses and tool calling
     * Supports: job execution, report pulling, system queries
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        List<Map<String, Object>> conversationHistory = (List<Map<String, Object>>) request.getOrDefault("conversationHistory", new ArrayList<>());
        List<Map<String, Object>> toolResults = (List<Map<String, Object>>) request.getOrDefault("toolResults", null);

        // Get current user
        String username = getCurrentUsername();

        // If we have tool results, provide final response
        if (toolResults != null && !toolResults.isEmpty()) {
            Map<String, Object> response = buildToolResponse(toolResults);
            return success(response);
        }

        // Check for specific commands
        Map<String, Object> commandResult = processCommands(message);
        if (commandResult != null) {
            return success(commandResult);
        }

        // Check if AI is available
        if (aiChatService != null && aiChatService.isEnabled()) {
            // Use AI for intelligent response
            try {
                String aiResponse = aiChatService.chat(message, username);

                // Check if AI response indicates tool usage
                List<Map<String, Object>> toolCalls = extractToolCalls(aiResponse, message);

                if (!toolCalls.isEmpty()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("toolCalls", toolCalls);
                    result.put("response", null);
                    return success(result);
                } else {
                    Map<String, Object> result = new HashMap<>();
                    result.put("response", aiResponse);
                    result.put("toolCalls", null);
                    return success(result);
                }
            } catch (Exception e) {
                // Fallback to rule-based if AI fails
                return success(processMessageFallback(message, null));
            }
        }

        // Fallback to rule-based processing
        return success(processMessageFallback(message, null));
    }

    /**
     * Process specific commands for jobs and reports
     */
    private Map<String, Object> processCommands(String message) {
        String msg = message.toLowerCase().trim();

        // Job execution commands
        if (msg.contains("run job") || msg.contains("execute job") || msg.contains("start job")) {
            return executeJobCommand(message);
        }

        // Report commands
        if (msg.contains("show report") || msg.contains("get report") || msg.contains("pull report") || msg.contains("run report")) {
            return executeReportCommand(message);
        }

        // System status commands
        if (msg.contains("system status") || msg.contains("job status") || msg.contains("how many jobs")) {
            return getSystemStatus(message);
        }

        return null;
    }

    /**
     * Execute job command
     */
    private Map<String, Object> executeJobCommand(String message) {
        try {
            // Extract job name from message
            String jobName = extractJobName(message);
            
            if (jdbcTemplate == null) {
                return createErrorResponse("Database not configured");
            }

            // Find job by name
            List<Map<String, Object>> jobs = jdbcTemplate.queryForList(
                "SELECT job_id, job_name, job_group, status FROM sys_job WHERE job_name LIKE ?",
                "%" + jobName + "%"
            );

            if (jobs.isEmpty()) {
                return createErrorResponse("Job not found: " + jobName);
            }

            Map<String, Object> job = jobs.get(0);
            Long jobId = ((Number) job.get("job_id")).longValue();
            String status = (String) job.get("status");

            if ("1".equals(status)) {
                return createErrorResponse("Job is paused. Please enable it first.");
            }

            // Note: Actual job execution would require Quartz scheduler integration
            // For now, return job details
            Map<String, Object> result = new HashMap<>();
            result.put("type", "job_execution");
            result.put("success", true);
            result.put("message", "Job found: " + job.get("job_name"));
            result.put("data", Map.of(
                "jobId", jobId,
                "jobName", job.get("job_name"),
                "jobGroup", job.get("job_group"),
                "status", "0".equals(status) ? "Active" : "Paused"
            ));

            return result;
        } catch (Exception e) {
            return createErrorResponse("Failed to execute job: " + e.getMessage());
        }
    }

    /**
     * Execute report command
     */
    private Map<String, Object> executeReportCommand(String message) {
        try {
            if (jdbcTemplate == null) {
                return createErrorResponse("Database not configured");
            }

            // Check for common report types
            if (message.toLowerCase().contains("user")) {
                List<Map<String, Object>> users = jdbcTemplate.queryForList(
                    "SELECT user_id, login_name, user_name, status, create_time FROM sys_user LIMIT 10"
                );

                Map<String, Object> result = new HashMap<>();
                result.put("type", "report");
                result.put("success", true);
                result.put("message", "User Report - Showing " + users.size() + " users");
                result.put("data", Map.of(
                    "reportName", "User List",
                    "rowCount", users.size(),
                    "columns", Arrays.asList("Login Name", "User Name", "Status", "Created"),
                    "rows", users.stream()
                        .map(u -> Arrays.asList(
                            u.get("login_name"),
                            u.get("user_name"),
                            "0".equals(u.get("status")) ? "Active" : "Disabled",
                            u.get("create_time")
                        ))
                        .toList()
                ));

                return result;
            }

            if (message.toLowerCase().contains("job")) {
                List<Map<String, Object>> jobs = jdbcTemplate.queryForList(
                    "SELECT job_id, job_name, cron_expression, status FROM sys_job LIMIT 10"
                );

                Map<String, Object> result = new HashMap<>();
                result.put("type", "report");
                result.put("success", true);
                result.put("message", "Job Report - Showing " + jobs.size() + " jobs");
                result.put("data", Map.of(
                    "reportName", "Scheduled Jobs",
                    "rowCount", jobs.size(),
                    "columns", Arrays.asList("Job Name", "Cron Expression", "Status"),
                    "rows", jobs.stream()
                        .map(j -> Arrays.asList(
                            j.get("job_name"),
                            j.get("cron_expression"),
                            "0".equals(j.get("status")) ? "Active" : "Paused"
                        ))
                        .toList()
                ));

                return result;
            }

            return createErrorResponse("Report type not recognized. Try 'user report' or 'job report'");
        } catch (Exception e) {
            return createErrorResponse("Failed to pull report: " + e.getMessage());
        }
    }

    /**
     * Get system status
     */
    private Map<String, Object> getSystemStatus(String message) {
        try {
            if (jdbcTemplate == null) {
                return createErrorResponse("Database not configured");
            }

            Map<String, Object> stats = new HashMap<>();

            // Get user count
            Number userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Number.class);
            stats.put("users", userCount != null ? userCount.intValue() : 0);

            // Get job count
            Number jobCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_job", Number.class);
            stats.put("jobs", jobCount != null ? jobCount.intValue() : 0);

            // Get active job count
            Number activeJobCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_job WHERE status='0'", Number.class);
            stats.put("activeJobs", activeJobCount != null ? activeJobCount.intValue() : 0);

            // Get role count
            Number roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role", Number.class);
            stats.put("roles", roleCount != null ? roleCount.intValue() : 0);

            Map<String, Object> result = new HashMap<>();
            result.put("type", "system_status");
            result.put("success", true);
            result.put("message", "System Status Overview");
            result.put("data", stats);

            return result;
        } catch (Exception e) {
            return createErrorResponse("Failed to get system status: " + e.getMessage());
        }
    }

    /**
     * Extract job name from message
     */
    private String extractJobName(String message) {
        // Simple extraction - look for words after "job"
        String[] words = message.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("job") && i + 1 < words.length) {
                return words[i + 1];
            }
        }
        return "test"; // default
    }

    /**
     * Create error response
     */
    private Map<String, Object> createErrorResponse(String errorMsg) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "error");
        result.put("success", false);
        result.put("message", errorMsg);
        return result;
    }

    /**
     * Get AI status
     */
    @GetMapping("/chat/status")
    public AjaxResult chatStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", aiChatService != null && aiChatService.isEnabled());
        status.put("knowledgeCount", knowledgeBaseService != null ? knowledgeBaseService.getStats().total() : 0);
        return success(status);
    }

    /**
     * Refresh knowledge base
     */
    @PostMapping("/chat/knowledge/refresh")
    public AjaxResult refreshKnowledge() {
        if (knowledgeBaseService != null && aiChatService != null) {
            knowledgeBaseService.initializeKnowledgeBase();
            return success("Knowledge base refreshed");
        }
        return error("Knowledge service not available");
    }

    /**
     * Get knowledge statistics
     */
    @GetMapping("/chat/knowledge/stats")
    public AjaxResult knowledgeStats() {
        if (knowledgeBaseService != null) {
            return success(knowledgeBaseService.getStats());
        }
        return success(Collections.emptyMap());
    }

    /**
     * Streaming chat endpoint (SSE)
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        String username = getCurrentUsername();
        SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout

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
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (IOException e) {
                            log.warn("Failed to send SSE token", e);
                        }
                    })
                    .doOnComplete(() -> emitter.complete())
                    .doOnError(throwable -> {
                        try {
                            emitter.send(SseEmitter.event().data("Error: " + throwable.getMessage()));
                        } catch (IOException e) {
                            // ignore
                        }
                        emitter.completeWithError(throwable);
                    })
                    .subscribe();
            } catch (Exception e) {
                log.error("Chat stream error", e);
                try {
                    emitter.send(SseEmitter.event().data("Internal error occurred"));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * Get conversation history for current user
     */
    @GetMapping("/chat/history")
    public AjaxResult getConversationHistory() {
        String username = getCurrentUsername();
        if (aiChatService != null) {
            return success(aiChatService.getUserConversationHistory(username));
        }
        return success(Collections.emptyList());
    }

    /**
     * Clear current user's conversation memory
     */
    @PostMapping("/chat/clear-memory")
    public AjaxResult clearMemory() {
        String username = getCurrentUsername();
        if (aiChatService != null) {
            aiChatService.clearMemory(username);
            return success("Conversation cleared");
        }
        return error("AI service not available");
    }

    /**
     * Extract tool calls from AI response or message intent
     */
    private List<Map<String, Object>> extractToolCalls(String aiResponse, String originalMessage) {
        List<Map<String, Object>> toolCalls = new ArrayList<>();
        String lowerMessage = originalMessage.toLowerCase();

        // Simple intent detection based on keywords
        if (lowerMessage.contains("create") && lowerMessage.contains("user")) {
            Map<String, Object> toolCall = new HashMap<>();
            toolCall.put("name", "createUser");
            toolCall.put("arguments", extractUserDetails(originalMessage));
            toolCalls.add(toolCall);
        } else if (lowerMessage.contains("list") && lowerMessage.contains("user")) {
            Map<String, Object> toolCall = new HashMap<>();
            toolCall.put("name", "listUsers");
            toolCall.put("arguments", new HashMap<>());
            toolCalls.add(toolCall);
        } else if (lowerMessage.contains("list") && lowerMessage.contains("role")) {
            Map<String, Object> toolCall = new HashMap<>();
            toolCall.put("name", "listRoles");
            toolCall.put("arguments", new HashMap<>());
            toolCalls.add(toolCall);
        } else if (lowerMessage.contains("create") && lowerMessage.contains("role")) {
            Map<String, Object> toolCall = new HashMap<>();
            toolCall.put("name", "createRole");
            toolCall.put("arguments", extractRoleDetails(originalMessage));
            toolCalls.add(toolCall);
        } else if (lowerMessage.contains("delete") && lowerMessage.contains("user")) {
            Map<String, Object> toolCall = new HashMap<>();
            toolCall.put("name", "deleteUser");
            toolCall.put("arguments", extractUserId(originalMessage));
            toolCalls.add(toolCall);
        }

        return toolCalls;
    }

    /**
     * Fallback message processing when AI is unavailable
     */
    private Map<String, Object> processMessageFallback(String message, List<Map<String, Object>> toolResults) {
        // If we have tool results, provide final response
        if (toolResults != null && !toolResults.isEmpty()) {
            return buildToolResponse(toolResults);
        }

        // Determine which tools to call based on message
        List<Map<String, Object>> toolCalls = extractToolCalls(null, message);

        if (!toolCalls.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("toolCalls", toolCalls);
            result.put("response", null);
            return result;
        } else {
            // Default response
            Map<String, Object> result = new HashMap<>();
            result.put("response", getGeneralResponse(message));
            result.put("toolCalls", null);
            return result;
        }
    }

    /**
     * Build response from tool execution results
     */
    private Map<String, Object> buildToolResponse(List<Map<String, Object>> toolResults) {
        Map<String, Object> result = new HashMap<>();
        StringBuilder response = new StringBuilder("I've completed the following actions:\n\n");
        
        for (Map<String, Object> toolResult : toolResults) {
            String toolName = (String) toolResult.get("name");
            Map<String, Object> toolData = (Map<String, Object>) toolResult.get("result");
            boolean success = toolData != null && Boolean.TRUE.equals(toolData.get("success"));

            if (success) {
                response.append("✅ ").append(formatToolName(toolName)).append(" - Success\n");

                // Add data summary for list operations
                if (toolName.equals("listUsers") || toolName.equals("listRoles")) {
                    Map<String, Object> data = (Map<String, Object>) toolData.get("data");
                    if (data != null && data.containsKey("data")) {
                        List<?> items = (List<?>) data.get("data");
                        if (items != null && !items.isEmpty()) {
                            response.append("   Found ").append(items.size()).append(" ").append(toolName.replace("list", "").toLowerCase()).append("\n");

                            // Show first few items
                            int showCount = Math.min(5, items.size());
                            for (int i = 0; i < showCount; i++) {
                                Map<String, Object> item = (Map<String, Object>) items.get(i);
                                if (toolName.equals("listUsers")) {
                                    String userName = (String) item.get("userName");
                                    String loginName = (String) item.get("loginName");
                                    response.append("   • ").append(userName).append(" (").append(loginName).append(")\n");
                                } else if (toolName.equals("listRoles")) {
                                    String roleName = (String) item.get("roleName");
                                    String roleKey = (String) item.get("roleKey");
                                    response.append("   • ").append(roleName).append(" (").append(roleKey).append(")\n");
                                }
                            }

                            if (items.size() > showCount) {
                                response.append("   ... and ").append(items.size() - showCount).append(" more\n");
                            }
                        } else {
                            response.append("   No ").append(toolName.replace("list", "").toLowerCase()).append(" found\n");
                        }
                    }
                }
            } else {
                response.append("❌ ").append(formatToolName(toolName)).append(" - Failed\n");
                if (toolData != null && toolData.containsKey("error")) {
                    response.append("   Error: ").append(toolData.get("error")).append("\n");
                }
            }
        }
        response.append("\nIs there anything else you'd like me to help with?");
        result.put("response", response.toString());
        return result;
    }

    /**
     * Extract user details from natural language message
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractUserDetails(String message) {
        Map<String, Object> details = new HashMap<>();

        // Generate login name from timestamp
        String baseLoginName = "user_" + System.currentTimeMillis() % 10000;
        details.put("loginName", baseLoginName);
        details.put("userName", "New User");
        details.put("password", "123456");
        details.put("email", "");
        details.put("phonenumber", "");
        details.put("sex", "0");
        details.put("status", "0");
        details.put("remark", "Created via AI Chat");

        // Try to extract name after "name" keyword
        String[] nameKeywords = {"name", "named", "called"};
        for (String keyword : nameKeywords) {
            if (message.toLowerCase().contains(keyword)) {
                int keywordIndex = message.toLowerCase().indexOf(keyword);
                String afterKeyword = message.substring(keywordIndex + keyword.length()).trim();

                // Get words until we hit a stop word or end
                String[] stopWords = {"and", "with", "for", "to", "the", "a", "an", "in", "at", "on"};
                String[] words = afterKeyword.split("\\s+");
                StringBuilder name = new StringBuilder();

                for (String word : words) {
                    String cleanWord = word.replaceAll("[^a-zA-Z]", "");
                    if (cleanWord.isEmpty()) continue;
                    if (isStopWord(cleanWord.toLowerCase(), stopWords)) break;

                    if (name.length() > 0) name.append(" ");
                    name.append(capitalize(cleanWord));
                }

                if (name.length() > 0) {
                    details.put("userName", name.toString());
                    // Generate login name from user name
                    String loginName = name.toString().toLowerCase().replaceAll("\\s+", ".");
                    details.put("loginName", loginName);
                    break;
                }
            }
        }

        return details;
    }

    /**
     * Check if word is a stop word
     */
    private boolean isStopWord(String word, String[] stopWords) {
        for (String stop : stopWords) {
            if (word.equals(stop)) return true;
        }
        return false;
    }

    /**
     * Extract role details from message
     */
    private Map<String, Object> extractRoleDetails(String message) {
        Map<String, Object> details = new HashMap<>();
        details.put("roleName", "New Role");
        details.put("roleKey", "role_" + System.currentTimeMillis() % 10000);
        details.put("roleSort", 0);
        details.put("status", "0");
        details.put("remark", "Created via AI Chat");

        if (message.contains("role")) {
            String[] parts = message.split("role");
            if (parts.length > 1) {
                String namePart = parts[1].trim();
                String[] words = namePart.split("\\s+");
                if (words.length > 0) {
                    StringBuilder name = new StringBuilder();
                    for (int i = 0; i < Math.min(3, words.length); i++) {
                        if (!words[i].matches("(and|with|for|to|the|a|an)")) {
                            if (name.length() > 0) name.append(" ");
                            name.append(words[i]);
                        }
                    }
                    if (name.length() > 0) {
                        details.put("roleName", capitalize(name.toString().replaceAll("[^a-zA-Z ]", "")));
                        details.put("roleKey", name.toString().toLowerCase().replaceAll("[^a-zA-Z]", ""));
                    }
                }
            }
        }

        return details;
    }

    /**
     * Extract user ID from message
     */
    private Map<String, Object> extractUserId(String message) {
        Map<String, Object> details = new HashMap<>();

        // Try to find number in message
        String[] words = message.split("\\s+");
        for (String word : words) {
            if (word.matches("\\d+")) {
                details.put("userId", Long.parseLong(word));
                return details;
            }
        }

        details.put("userId", 1); // Default
        return details;
    }

    /**
     * Format tool name for display
     */
    private String formatToolName(String name) {
        return name.replaceAll("(\\p{Lu})", " $1").trim();
    }

    /**
     * Get general response for non-tool messages
     */
    private String getGeneralResponse(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "Hello! I'm your Vantage Admin assistant. I can help you:\n\n" +
                   "• Create and manage users\n" +
                   "• Manage roles and permissions\n" +
                   "• Configure system settings\n" +
                   "• View system information\n\n" +
                   "What would you like to do?";
        } else if (lowerMessage.contains("help")) {
            return "I can help you with:\n\n" +
                   "1. **User Management**\n" +
                   "   - \"Create a user with name John Doe\"\n" +
                   "   - \"List all users\"\n" +
                   "   - \"Delete user 123\"\n\n" +
                   "2. **Role Management**\n" +
                   "   - \"Create a role named Admin\"\n" +
                   "   - \"List all roles\"\n\n" +
                   "Just ask me in natural language!";
        } else if (lowerMessage.contains("thank")) {
            return "You're welcome! Is there anything else I can help you with?";
        } else {
            return "I understand you're asking about: \"" + message + "\"\n\n" +
                   "I can help you create users, manage roles, and configure the system. " +
                   "Try asking me to:\n" +
                   "• \"Create a user with name John Doe\"\n" +
                   "• \"List all users\"\n" +
                   "• \"Create a role named Manager\"";
        }
    }

    /**
     * Capitalize first letter of string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Get current username from security context
     */
    private String getCurrentUsername() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof LoginUser loginUser) {
                return loginUser.getUsername();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "anonymous";
    }
}
