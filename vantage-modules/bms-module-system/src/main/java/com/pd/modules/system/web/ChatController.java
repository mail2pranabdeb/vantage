package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Chat controller with MCP tool integration
 */
@RestController
@RequestMapping("/api")
public class ChatController extends BaseController {

    /**
     * Chat endpoint with tool calling support
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        List<Map<String, Object>> conversationHistory = (List<Map<String, Object>>) request.getOrDefault("conversationHistory", new ArrayList<>());
        List<Map<String, Object>> toolResults = (List<Map<String, Object>>) request.getOrDefault("toolResults", null);

        // Simple rule-based response for demo
        Map<String, Object> response = processMessage(message, toolResults);

        return success(response);
    }

    /**
     * Process message and determine if tools need to be called
     */
    private Map<String, Object> processMessage(String message, List<Map<String, Object>> toolResults) {
        Map<String, Object> result = new HashMap<>();
        String lowerMessage = message.toLowerCase();

        // If we have tool results, provide final response
        if (toolResults != null && !toolResults.isEmpty()) {
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

        // Determine which tools to call based on message
        List<Map<String, Object>> toolCalls = new ArrayList<>();

        if (lowerMessage.contains("create") && lowerMessage.contains("user")) {
            // Extract user details from message
            Map<String, Object> toolCall = new HashMap<>();
            toolCall.put("name", "createUser");
            Map<String, Object> args = extractUserDetails(message);
            toolCall.put("arguments", args);
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
            Map<String, Object> args = extractRoleDetails(message);
            toolCall.put("arguments", args);
            toolCalls.add(toolCall);
        } else if (lowerMessage.contains("delete") && lowerMessage.contains("user")) {
            Map<String, Object> toolCall = new HashMap<>();
            toolCall.put("name", "deleteUser");
            Map<String, Object> args = extractUserId(message);
            toolCall.put("arguments", args);
            toolCalls.add(toolCall);
        }

        if (!toolCalls.isEmpty()) {
            result.put("toolCalls", toolCalls);
            result.put("response", null);
        } else {
            // Default response
            result.put("response", getGeneralResponse(message));
            result.put("toolCalls", null);
        }

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
}
