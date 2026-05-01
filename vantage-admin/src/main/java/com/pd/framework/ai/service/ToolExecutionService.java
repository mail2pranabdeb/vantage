package com.pd.framework.ai.service;

import com.pd.modules.quartz.api.QuartzJobService;
import com.pd.modules.system.api.SystemConfigService;
import com.pd.modules.system.api.SystemOperLogService;
import com.pd.modules.system.api.SystemRoleService;
import com.pd.modules.system.api.SystemUserService;
import com.pd.modules.system.api.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool Execution Service for AI Chat
 * Maps AI tool calls to actual system operations
 */
@Service
public class ToolExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionService.class);

    private final SystemUserService systemUserService;
    private final SystemRoleService systemRoleService;
    private final QuartzJobService quartzJobService;
    private final SystemOperLogService systemOperLogService;
    private final SystemConfigService systemConfigService;

    public ToolExecutionService(
            SystemUserService systemUserService,
            SystemRoleService systemRoleService,
            QuartzJobService quartzJobService,
            SystemOperLogService systemOperLogService,
            SystemConfigService systemConfigService) {
        this.systemUserService = systemUserService;
        this.systemRoleService = systemRoleService;
        this.quartzJobService = quartzJobService;
        this.systemOperLogService = systemOperLogService;
        this.systemConfigService = systemConfigService;
    }

    /**
     * Execute a tool call by name with arguments
     */
    public Map<String, Object> executeTool(String toolName, Map<String, Object> arguments) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", toolName);

        try {
            switch (toolName) {
                case "createUser":
                    result.put("result", executeCreateUser(arguments));
                    break;
                case "listUsers":
                    result.put("result", executeListUsers(arguments));
                    break;
                case "updateUser":
                    result.put("result", executeUpdateUser(arguments));
                    break;
                case "deleteUser":
                    result.put("result", executeDeleteUser(arguments));
                    break;
                case "listRoles":
                    result.put("result", executeListRoles(arguments));
                    break;
                case "createRole":
                    result.put("result", executeCreateRole(arguments));
                    break;
                case "executeJob":
                    result.put("result", executeJob(arguments));
                    break;
                case "listJobs":
                    result.put("result", executeListJobs(arguments));
                    break;
                case "getOperationLogs":
                    result.put("result", executeGetOperationLogs(arguments));
                    break;
                case "getConfig":
                    result.put("result", executeGetConfig(arguments));
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "Unknown tool: " + toolName);
                    break;
            }
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolName, e);
            result.put("success", false);
            result.put("message", "Execution error: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> executeCreateUser(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        String loginName = (String) args.get("loginName");
        String userName = (String) args.get("userName");
        String password = (String) args.getOrDefault("password", "123456");
        String email = (String) args.get("email");

        if (loginName == null || loginName.isEmpty()) {
            result.put("success", false);
            result.put("message", "loginName is required");
            return result;
        }
        if (userName == null || userName.isEmpty()) {
            result.put("success", false);
            result.put("message", "userName is required");
            return result;
        }
        if (systemUserService.existsByLoginName(loginName)) {
            result.put("success", false);
            result.put("message", "User already exists: " + loginName);
            return result;
        }

        UserDTO user = new UserDTO();
        user.setLoginName(loginName);
        user.setUserName(userName);
        user.setPassword(password);
        user.setEmail(email);
        user.setUserType("00");
        user.setSex("0");
        user.setStatus("0");
        user.setCreateBy("ai-assistant");

        systemUserService.createUser(user);
        result.put("success", true);
        result.put("message", "User created: " + loginName);
        result.put("userId", loginName);
        return result;
    }

    private Map<String, Object> executeListUsers(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        List<?> users = systemUserService.findAllActive();
        result.put("success", true);
        result.put("count", users.size());
        result.put("users", users);
        return result;
    }

    private Map<String, Object> executeUpdateUser(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        Long userId = args.get("userId") != null ? ((Number) args.get("userId")).longValue() : null;
        String status = (String) args.get("status");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "userId is required");
            return result;
        }

        var userOpt = systemUserService.findById(userId);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "User not found: " + userId);
            return result;
        }

        var user = userOpt.get();
        if (status != null) {
            user.setStatus(status);
            user.setUpdateBy("ai-assistant");
            systemUserService.updateUser(user);
        }

        result.put("success", true);
        result.put("message", "User updated: " + userId);
        return result;
    }

    private Map<String, Object> executeDeleteUser(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        Long userId = args.get("userId") != null ? ((Number) args.get("userId")).longValue() : null;

        if (userId == null) {
            result.put("success", false);
            result.put("message", "userId is required");
            return result;
        }

        boolean deleted = systemUserService.deleteUser(userId);
        result.put("success", deleted);
        result.put("message", deleted ? "User deleted: " + userId : "User not found");
        return result;
    }

    private Map<String, Object> executeListRoles(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        List<?> roles = systemRoleService.findAllActive();
        result.put("success", true);
        result.put("count", roles.size());
        result.put("roles", roles);
        return result;
    }

    private Map<String, Object> executeCreateRole(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        String roleName = (String) args.get("roleName");
        String roleKey = (String) args.get("roleKey");

        if (roleName == null || roleKey == null) {
            result.put("success", false);
            result.put("message", "roleName and roleKey are required");
            return result;
        }

        if (systemRoleService.findByRoleKey(roleKey).isPresent()) {
            result.put("success", false);
            result.put("message", "Role already exists: " + roleKey);
            return result;
        }

        var role = new com.pd.modules.system.api.dto.RoleDTO();
        role.setRoleName(roleName);
        role.setRoleKey(roleKey);
        role.setDataScope("1");
        role.setStatus("0");
        role.setDelFlag("0");
        role.setCreateBy("ai-assistant");

        systemRoleService.createRole(role);
        result.put("success", true);
        result.put("message", "Role created: " + roleName);
        return result;
    }

    private Map<String, Object> executeJob(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        Long jobId = args.get("jobId") != null ? ((Number) args.get("jobId")).longValue() : null;
        String jobName = (String) args.get("jobName");

        if (jobId != null) {
            quartzJobService.runJob(jobId);
            result.put("success", true);
            result.put("message", "Job executed: " + jobId);
        } else if (jobName != null) {
            var jobs = quartzJobService.findAll().stream()
                    .filter(j -> j.getJobName().contains(jobName))
                    .toList();
            if (jobs.isEmpty()) {
                result.put("success", false);
                result.put("message", "Job not found: " + jobName);
            } else {
                quartzJobService.runJob(jobs.get(0).getJobId());
                result.put("success", true);
                result.put("message", "Job executed: " + jobs.get(0).getJobName());
            }
        } else {
            result.put("success", false);
            result.put("message", "jobId or jobName is required");
        }
        return result;
    }

    private Map<String, Object> executeListJobs(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        List<?> jobs = quartzJobService.findAll();
        result.put("success", true);
        result.put("count", jobs.size());
        result.put("jobs", jobs);
        return result;
    }

    private Map<String, Object> executeGetOperationLogs(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        String title = (String) args.get("title");
        String operName = (String) args.get("operName");
        List<?> logs = systemOperLogService.findByCondition(title, operName, null, null);
        result.put("success", true);
        result.put("count", logs.size());
        result.put("logs", logs);
        return result;
    }

    private Map<String, Object> executeGetConfig(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        String configKey = (String) args.get("configKey");

        if (configKey == null) {
            List<?> configs = systemConfigService.findAll();
            result.put("success", true);
            result.put("count", configs.size());
            result.put("configs", configs);
        } else {
            var configOpt = systemConfigService.findByConfigKey(configKey);
            if (configOpt.isPresent()) {
                result.put("success", true);
                result.put("config", configOpt.get());
            } else {
                result.put("success", false);
                result.put("message", "Config not found: " + configKey);
            }
        }
        return result;
    }
}
