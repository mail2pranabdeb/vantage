package com.pd.gateway;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.framework.security.jwt.JwtTokenUtil;
import com.pd.framework.security.jwt.LoginRequest;
import com.pd.framework.security.jwt.LoginResponse;
import com.pd.modules.generator.api.GeneratorService;
import com.pd.modules.generator.api.dto.CloneTableRequest;
import com.pd.modules.generator.api.dto.CreateTableRequest;
import com.pd.modules.generator.service.GenService;
import com.pd.modules.quartz.api.*;
import com.pd.modules.quartz.api.dto.EmailTemplateDTO;
import com.pd.modules.quartz.api.dto.JobDTO;
import com.pd.modules.quartz.api.dto.JobLogDTO;
import com.pd.modules.system.api.*;
import com.pd.modules.system.api.dto.*;
import com.pd.modules.system.report.api.ReportDesignerService;
import com.pd.modules.system.report.domain.SysReportTemplate;
import com.pd.modules.system.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Gateway Management Controller - sole external HTTP entry point.
 * All requests route through module API interfaces only.
 * No module controller is directly exposed.
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public class GatewayManagement extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(GatewayManagement.class);

    // System module APIs
    private final SystemAuthService systemAuthService;
    private final SystemUserService systemUserService;
    private final SystemRoleService systemRoleService;
    private final SystemMenuService systemMenuService;
    private final SystemDictService systemDictService;
    private final SystemConfigService systemConfigService;
    private final SystemOperLogService systemOperLogService;
    private final SystemLogininforService systemLogininforService;
    private final SystemNoticeService systemNoticeService;
    private final SystemNotificationService systemNotificationService;
    private final SystemDatasourceService systemDatasourceService;
    private final SystemEmailConfigService systemEmailConfigService;
    private final SystemCacheService systemCacheService;
    private final SystemChatService systemChatService;
    private final SystemReportEntityService systemReportEntityService;
    private final ReportDesignerService reportDesignerService;

    // Quartz module APIs
    private final QuartzJobService quartzJobService;
    private final QuartzJobLogService quartzJobLogService;
    private final QuartzJobTemplateService quartzJobTemplateService;
    private final QuartzEmailJobTemplateService quartzEmailJobTemplateService;
    private final QuartzJobGroupService quartzJobGroupService;
    private final QuartzJobMetricsService quartzJobMetricsService;
    private final QuartzScriptJobService quartzScriptJobService;
    private final QuartzJobWebhookService quartzJobWebhookService;

    // Generator module API
    private final GeneratorService generatorService;
    private final GenService genService;

    // Authentication
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    public GatewayManagement(
            SystemAuthService systemAuthService,
            SystemUserService systemUserService,
            SystemRoleService systemRoleService,
            SystemMenuService systemMenuService,
            SystemDictService systemDictService,
            SystemConfigService systemConfigService,
            SystemOperLogService systemOperLogService,
            SystemLogininforService systemLogininforService,
            SystemNoticeService systemNoticeService,
            SystemNotificationService systemNotificationService,
            SystemDatasourceService systemDatasourceService,
            SystemEmailConfigService systemEmailConfigService,
            SystemCacheService systemCacheService,
            SystemChatService systemChatService,
            SystemReportEntityService systemReportEntityService,
            ReportDesignerService reportDesignerService,
            QuartzJobService quartzJobService,
            QuartzJobLogService quartzJobLogService,
            QuartzJobTemplateService quartzJobTemplateService,
            QuartzEmailJobTemplateService quartzEmailJobTemplateService,
            QuartzJobGroupService quartzJobGroupService,
            QuartzJobMetricsService quartzJobMetricsService,
            QuartzScriptJobService quartzScriptJobService,
            QuartzJobWebhookService quartzJobWebhookService,
            GeneratorService generatorService,
            GenService genService,
            AuthenticationManager authenticationManager,
            JwtTokenUtil jwtTokenUtil) {
        this.systemAuthService = systemAuthService;
        this.systemUserService = systemUserService;
        this.systemRoleService = systemRoleService;
        this.systemMenuService = systemMenuService;
        this.systemDictService = systemDictService;
        this.systemConfigService = systemConfigService;
        this.systemOperLogService = systemOperLogService;
        this.systemLogininforService = systemLogininforService;
        this.systemNoticeService = systemNoticeService;
        this.systemNotificationService = systemNotificationService;
        this.systemDatasourceService = systemDatasourceService;
        this.systemEmailConfigService = systemEmailConfigService;
        this.systemCacheService = systemCacheService;
        this.systemChatService = systemChatService;
        this.systemReportEntityService = systemReportEntityService;
        this.reportDesignerService = reportDesignerService;
        this.quartzJobService = quartzJobService;
        this.quartzJobLogService = quartzJobLogService;
        this.quartzJobTemplateService = quartzJobTemplateService;
        this.quartzEmailJobTemplateService = quartzEmailJobTemplateService;
        this.quartzJobGroupService = quartzJobGroupService;
        this.quartzJobMetricsService = quartzJobMetricsService;
        this.quartzScriptJobService = quartzScriptJobService;
        this.quartzJobWebhookService = quartzJobWebhookService;
        this.generatorService = generatorService;
        this.genService = genService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    // ========== Auth ==========

    @Tag(name = "Authentication", description = "User authentication with JWT tokens and OAuth2")
    @Tag(name = "Authentication", description = "User authentication with JWT tokens and OAuth2")
    @PostMapping("/login")
    @Operation(summary = "Login with credentials", description = "Authenticates user and returns JWT access + refresh tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, tokens returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public AjaxResult login(@RequestBody LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            String token = jwtTokenUtil.generateToken(loginUser);
            String refreshToken = jwtTokenUtil.generateRefreshToken(loginUser);

            return success(new LoginResponse(
                    token, refreshToken, "Bearer",
                    86400000, loginUser.getUsername()));
        } catch (Exception e) {
            log.error("Login failed for {}: {} - {}", request.getUsername(), e.getClass().getSimpleName(), e.getMessage());
            return AjaxResult.error(401, "Invalid credentials");
        }
    }

    @Tag(name = "Authentication", description = "User authentication with JWT tokens and OAuth2")
    @Tag(name = "Authentication")
    @PostMapping("/login/refresh")
    @Operation(summary = "Refresh JWT token", description = "Issues a new access token using a valid refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public AjaxResult refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return error("Refresh token is required");
        }
        try {
            String username = jwtTokenUtil.extractUsername(refreshToken);
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(username, "", new java.util.ArrayList<>());
            if (jwtTokenUtil.isTokenValid(refreshToken, userDetails)) {
                String newToken = jwtTokenUtil.generateToken(userDetails);
                return success(new LoginResponse(newToken, refreshToken, "Bearer", 86400000, username));
            }
            return AjaxResult.error(401, "Invalid refresh token");
        } catch (Exception e) {
            return AjaxResult.error(401, "Invalid refresh token");
        }
    }

    @Tag(name = "Authentication", description = "User authentication with JWT tokens and OAuth2")
    @Tag(name = "Authentication")
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public AjaxResult me() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return AjaxResult.error(401, "Not authenticated");
        }
        return success(systemAuthService.getCurrentUser());
    }
    @Tag(name = "Authentication")
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logs out the current user and invalidates session")
    @ApiResponse(responseCode = "200", description = "Logged out successfully")
    public AjaxResult logout() {
        return success(systemAuthService.logout());
    }

    // ========== System: User ==========

    @Tag(name = "System - User Management", description = "User CRUD operations and user-related queries")
    @GetMapping("/system/user/list")
    @Operation(summary = "List all active users", description = "Returns paginated list of active users with optional filters")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    public AjaxResult listUsers(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "Filter by login name") @RequestParam(required = false) String loginName,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize);
        var page = systemUserService.searchUsers(loginName, status, pageable);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        return success(result);
    }
    @Tag(name = "System - User Management")
    @GetMapping("/system/user/{userId}")
    @Operation(summary = "Get user by ID", description = "Returns a specific user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "500", description = "User not found")
    })
    public AjaxResult getUser(@Parameter(description = "User ID") @PathVariable Long userId) {
        return systemUserService.findById(userId)
                .map(this::success)
                .orElseGet(() -> error("User not found"));
    }
    @Tag(name = "System - User Management")
    @PostMapping("/system/user")
    @Operation(summary = "Create user", description = "Creates a new user account")
    @ApiResponse(responseCode = "200", description = "User created successfully")
    public AjaxResult addUser(@RequestBody UserDTO user) {
        if (systemUserService.existsByLoginName(user.getLoginName())) {
            return error("Login name already exists");
        }
        user.setUserType("00");
        user.setSex(user.getSex() != null ? user.getSex() : "0");
        user.setStatus(user.getStatus() != null ? user.getStatus() : "0");
        user.setCreateBy("admin");
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }
        systemUserService.createUser(user);
        return success("User added successfully");
    }
    @Tag(name = "System - User Management")
    @PutMapping("/system/user")
    @Operation(summary = "Update user", description = "Updates an existing user account")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    public AjaxResult updateUser(@RequestBody UserDTO user) {
        if (systemUserService.findById(user.getUserId()).isEmpty()) {
            return error("User not found");
        }
        user.setUpdateBy("admin");
        systemUserService.updateUser(user);
        return success("User updated successfully", systemUserService.findById(user.getUserId()).orElse(null));
    }
    @Tag(name = "System - User Management")
    @DeleteMapping("/system/user/{userId}")
    @Operation(summary = "Delete user", description = "Deletes a user by ID")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    public AjaxResult removeUser(@Parameter(description = "User ID") @PathVariable Long userId) {
        if (!systemUserService.deleteUser(userId)) {
            return error("User not found");
        }
        return success("User deleted successfully");
    }

    // ========== System: Role ==========

    @Tag(name = "System - Role Management", description = "Role CRUD operations and permission management")
    @GetMapping("/system/role/list")
    @Operation(summary = "List all active roles", description = "Returns a list of all active roles")
    @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    public AjaxResult listRoles() {
        return success(systemRoleService.findAllActive());
    }
    @Tag(name = "System - Role Management")
    @GetMapping("/system/role/{roleId}")
    @Operation(summary = "Get role by ID", description = "Returns a specific role by its ID")
    public AjaxResult getRole(@Parameter(description = "Role ID") @PathVariable Long roleId) {
        return systemRoleService.findById(roleId)
                .map(this::success)
                .orElseGet(() -> error("Role not found"));
    }
    @Tag(name = "System - Role Management")
    @PostMapping("/system/role")
    @Operation(summary = "Create role", description = "Creates a new role with permissions")
    @ApiResponse(responseCode = "200", description = "Role created successfully")
    public AjaxResult addRole(@RequestBody RoleDTO role) {
        if (systemRoleService.findByRoleKey(role.getRoleKey()).isPresent()) {
            return error("Role key already exists");
        }
        role.setDataScope("1");
        role.setStatus(role.getStatus() != null ? role.getStatus() : "0");
        role.setDelFlag("0");
        role.setCreateBy("admin");
        systemRoleService.createRole(role);
        return success("Role added successfully");
    }
    @Tag(name = "System - Role Management")
    @PutMapping("/system/role")
    @Operation(summary = "Update role", description = "Updates an existing role")
    @ApiResponse(responseCode = "200", description = "Role updated successfully")
    public AjaxResult updateRole(@RequestBody RoleDTO role) {
        if (systemRoleService.findById(role.getRoleId()).isEmpty()) {
            return error("Role not found");
        }
        role.setUpdateBy("admin");
        systemRoleService.updateRole(role);
        return success("Role updated successfully");
    }
    @Tag(name = "System - Role Management")
    @DeleteMapping("/system/role/{roleId}")
    @Operation(summary = "Delete role", description = "Deletes a role by ID")
    @ApiResponse(responseCode = "200", description = "Role deleted successfully")
    public AjaxResult removeRole(@Parameter(description = "Role ID") @PathVariable Long roleId) {
        if (!systemRoleService.deleteRole(roleId)) {
            return error("Role not found");
        }
        return success("Role deleted successfully");
    }

    // ========== System: Menu ==========

    @Tag(name = "System - Menu Management", description = "Navigation menu and menu tree operations")
    @GetMapping("/system/menu/tree")
    @Operation(summary = "Get menu tree", description = "Returns the complete menu hierarchy as a tree structure")
    @ApiResponse(responseCode = "200", description = "Menu tree retrieved successfully")
    public AjaxResult getMenuTree() {
        List<MenuDTO> menus = systemMenuService.findAllMenus();
        return success(buildMenuTree(menus, 0L));
    }
    @Tag(name = "System - Menu Management")
    @GetMapping("/system/menu/list")
    @Operation(summary = "List all menus", description = "Returns a flat list of all active menus")
    @ApiResponse(responseCode = "200", description = "Menus retrieved successfully")
    public AjaxResult listMenus() {
        return success(systemMenuService.findAllActive());
    }
    @Tag(name = "System - Menu Management")
    @GetMapping("/system/menu/{menuId}")
    @Operation(summary = "Get menu by ID", description = "Returns a specific menu item")
    public AjaxResult getMenu(@Parameter(description = "Menu ID") @PathVariable Long menuId) {
        return systemMenuService.findById(menuId)
                .map(this::success)
                .orElseGet(() -> error("Menu not found"));
    }
    @Tag(name = "System - Menu Management")
    @PostMapping("/system/menu")
    @Operation(summary = "Create menu", description = "Creates a new menu item")
    @ApiResponse(responseCode = "200", description = "Menu created successfully")
    public AjaxResult addMenu(@RequestBody MenuDTO menu) {
        menu.setVisible(menu.getVisible() != null ? menu.getVisible() : "0");
        menu.setMenuType(menu.getMenuType() != null ? menu.getMenuType() : "M");
        menu.setStatus("0");
        menu.setCreateBy("admin");
        if (menu.getParentId() == null) menu.setParentId(0L);
        if (menu.getOrderNum() == null) menu.setOrderNum(0);
        systemMenuService.createMenu(menu);
        return success("Menu added successfully");
    }
    @Tag(name = "System - Menu Management")
    @PutMapping("/system/menu")
    @Operation(summary = "Update menu", description = "Updates an existing menu item")
    @ApiResponse(responseCode = "200", description = "Menu updated successfully")
    public AjaxResult updateMenu(@RequestBody MenuDTO menu) {
        if (systemMenuService.findById(menu.getMenuId()).isEmpty()) {
            return error("Menu not found");
        }
        menu.setUpdateBy("admin");
        systemMenuService.updateMenu(menu);
        return success("Menu updated successfully");
    }
    @Tag(name = "System - Menu Management")
    @DeleteMapping("/system/menu/{menuId}")
    @Operation(summary = "Delete menu", description = "Deletes a menu item by ID")
    @ApiResponse(responseCode = "200", description = "Menu deleted successfully")
    public AjaxResult removeMenu(@Parameter(description = "Menu ID") @PathVariable Long menuId) {
        if (systemMenuService.findById(menuId).isEmpty()) {
            return error("Menu not found");
        }
        systemMenuService.deleteMenu(menuId);
        return success("Menu deleted successfully");
    }

    private List<MenuDTO> buildMenuTree(List<MenuDTO> menus, Long parentId) {
        for (MenuDTO menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                menu.setChildren(buildMenuTree(menus, menu.getMenuId()));
            }
        }
        return menus.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .toList();
    }

    // ========== System: Dict ==========

    @Tag(name = "System - Dictionary", description = "Data dictionary types and values management")
    @GetMapping("/system/dict/type/list")
    @Operation(summary = "List dictionary types", description = "Returns all active dictionary types")
    @ApiResponse(responseCode = "200", description = "Dictionary types retrieved")
    public AjaxResult listDictTypes() {
        return success(systemDictService.findAllActiveTypes());
    }
    @Tag(name = "System - Dictionary")
    @GetMapping("/system/dict/data/list")
    @Operation(summary = "List dictionary data by type", description = "Returns dictionary data for a specific type, sorted")
    @ApiResponse(responseCode = "200", description = "Dictionary data retrieved")
    public AjaxResult listDictData(@Parameter(description = "Dictionary type code") @RequestParam String dictType) {
        return success(systemDictService.findDataByTypeOrderBySort(dictType));
    }
    @Tag(name = "System - Dictionary")
    @GetMapping("/system/dict/data/type/{dictType}")
    @Operation(summary = "Get dictionary data by type path", description = "Returns dictionary data for a specific type")
    public AjaxResult getDictDataByType(@Parameter(description = "Dictionary type code") @PathVariable String dictType) {
        return success(systemDictService.findDataByTypeOrderBySort(dictType));
    }
    @Tag(name = "System - Dictionary")
    @GetMapping("/system/dict/type/get-by-code/{dictType}")
    @Operation(summary = "Get dictionary type by code", description = "Returns a dictionary type by its code")
    public AjaxResult getDictTypeByCode(@Parameter(description = "Dictionary type code") @PathVariable String dictType) {
        return systemDictService.findTypeByDictType(dictType)
                .map(this::success)
                .orElse(success(null));
    }
    @Tag(name = "System - Dictionary")
    @GetMapping("/system/dict/type/{dictId}")
    @Operation(summary = "Get dictionary type by ID", description = "Returns a specific dictionary type")
    public AjaxResult getDictType(@Parameter(description = "Dictionary type ID") @PathVariable Long dictId) {
        return systemDictService.findTypeById(dictId)
                .map(this::success)
                .orElseGet(() -> error("Dictionary type not found"));
    }
    @Tag(name = "System - Dictionary")
    @PostMapping("/system/dict/type")
    @Operation(summary = "Create dictionary type", description = "Creates a new dictionary type")
    @ApiResponse(responseCode = "200", description = "Dictionary type created")
    public AjaxResult addDictType(@RequestBody DictTypeDTO dict) {
        dict.setStatus(dict.getStatus() != null ? dict.getStatus() : "0");
        dict.setCreateBy("admin");
        dict.setCreateTime(java.time.LocalDateTime.now());
        systemDictService.createType(dict);
        return success("Dictionary type added successfully");
    }
    @Tag(name = "System - Dictionary")
    @PutMapping("/system/dict/type")
    @Operation(summary = "Update dictionary type", description = "Updates an existing dictionary type")
    @ApiResponse(responseCode = "200", description = "Dictionary type updated")
    public AjaxResult updateDictType(@RequestBody DictTypeDTO dict) {
        if (systemDictService.findTypeById(dict.getDictId()).isEmpty()) {
            return error("Dictionary type not found");
        }
        dict.setUpdateBy("admin");
        dict.setUpdateTime(java.time.LocalDateTime.now());
        systemDictService.updateType(dict);
        return success("Dictionary type updated successfully");
    }
    @Tag(name = "System - Dictionary")
    @DeleteMapping("/system/dict/type/{dictId}")
    @Operation(summary = "Delete dictionary type", description = "Deletes a dictionary type by ID")
    @ApiResponse(responseCode = "200", description = "Dictionary type deleted")
    public AjaxResult removeDictType(@Parameter(description = "Dictionary type ID") @PathVariable Long dictId) {
        if (systemDictService.findTypeById(dictId).isEmpty()) {
            return error("Dictionary type not found");
        }
        systemDictService.deleteTypeById(dictId);
        return success("Dictionary type deleted successfully");
    }
    @Tag(name = "System - Dictionary")
    @PostMapping("/system/dict/data")
    @Operation(summary = "Create dictionary data", description = "Creates a new dictionary data entry")
    @ApiResponse(responseCode = "200", description = "Dictionary data created")
    public AjaxResult addDictData(@RequestBody DictDataDTO dictData) {
        dictData.setStatus(dictData.getStatus() != null ? dictData.getStatus() : "0");
        dictData.setCreateBy("admin");
        dictData.setCreateTime(java.time.LocalDateTime.now());
        systemDictService.createData(dictData);
        return success("Dictionary data added successfully");
    }
    @Tag(name = "System - Dictionary")
    @PutMapping("/system/dict/data")
    @Operation(summary = "Update dictionary data", description = "Updates an existing dictionary data entry")
    @ApiResponse(responseCode = "200", description = "Dictionary data updated")
    public AjaxResult updateDictData(@RequestBody DictDataDTO dictData) {
        if (dictData.getDictCode() == null || systemDictService.findDataById(dictData.getDictCode()).isEmpty()) {
            return error("Dictionary data not found");
        }
        dictData.setUpdateBy("admin");
        dictData.setUpdateTime(java.time.LocalDateTime.now());
        systemDictService.updateData(dictData);
        return success("Dictionary data updated successfully");
    }
    @Tag(name = "System - Dictionary")
    @DeleteMapping("/system/dict/data/{dictCode}")
    @Operation(summary = "Delete dictionary data", description = "Deletes a dictionary data entry")
    @ApiResponse(responseCode = "200", description = "Dictionary data deleted")
    public AjaxResult removeDictData(@Parameter(description = "Dictionary data code") @PathVariable Long dictCode) {
        if (systemDictService.findDataById(dictCode).isEmpty()) {
            return error("Dictionary data not found");
        }
        systemDictService.deleteDataById(dictCode);
        return success("Dictionary data deleted successfully");
    }

    // ========== System: Config ==========

    @Tag(name = "System - Configuration", description = "System configuration settings")
    @GetMapping("/system/config/list")
    @Operation(summary = "List all configurations", description = "Returns all system configuration entries")
    @ApiResponse(responseCode = "200", description = "Configurations retrieved")
    public AjaxResult listConfigs() {
        return success(systemConfigService.findAll());
    }
    @Tag(name = "System - Configuration")
    @PostMapping("/system/config/batch")
    @Operation(summary = "Batch save configurations", description = "Creates or updates multiple configuration entries")
    @ApiResponse(responseCode = "200", description = "Configurations saved")
    public AjaxResult batchSaveConfigs(@RequestBody List<Map<String, Object>> configs) {
        int savedCount = 0;
        for (Map<String, Object> configData : configs) {
            String configKey = (String) configData.get("configKey");
            String configValue = (String) configData.get("configValue");
            if (configKey != null && configValue != null) {
                if (systemConfigService.existsByConfigKey(configKey)) {
                    var existing = systemConfigService.findByConfigKey(configKey).orElse(null);
                    if (existing != null) {
                        existing.setConfigValue(configValue);
                        existing.setUpdateBy("admin");
                        systemConfigService.updateConfig(existing);
                    }
                } else {
                    ConfigDTO newConfig = new ConfigDTO();
                    newConfig.setConfigName(configKey.replace('.', ' ').replace('_', ' '));
                    newConfig.setConfigKey(configKey);
                    newConfig.setConfigValue(configValue);
                    newConfig.setConfigType("Y");
                    newConfig.setCreateBy("admin");
                    newConfig.setUpdateBy("admin");
                    newConfig.setRemark("System setting");
                    systemConfigService.createConfig(newConfig);
                }
                savedCount++;
            }
        }
        return success("Saved " + savedCount + " configuration(s)");
    }
    @Tag(name = "System - Configuration")
    @PostMapping("/system/config/test-email")
    @Operation(summary = "Test email configuration", description = "Sends a test email with provided SMTP settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test email sent or error message"),
        @ApiResponse(responseCode = "400", description = "Missing required fields")
    })
    public AjaxResult testEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String host = request.get("host");
        String port = request.get("port");
        String username = request.get("username");
        String password = request.get("password");
        String enableAuth = request.get("enableAuth");
        String enableTls = request.get("enableTls");
        if (to == null || to.isEmpty()) return error("Recipient email is required");
        if (host == null || host.isEmpty()) return error("SMTP host is required");
        try {
            org.springframework.mail.javamail.JavaMailSenderImpl mailSender = new org.springframework.mail.javamail.JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(Integer.parseInt(port != null ? port : "587"));
            mailSender.setUsername(username != null ? username : "");
            mailSender.setPassword(password != null ? password : "");
            java.util.Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true".equals(enableAuth));
            if ("true".equals(enableTls)) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            } else {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", port != null ? port : "465");
                props.put("mail.smtp.socketFactory.fallback", "false");
            }
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "30000");
            props.put("mail.debug", "false");
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom("Vantage Admin <test@vantage.com>");
            message.setTo(to);
            message.setSubject("Test Email from Vantage Admin");
            message.setText("This is a test email to verify your SMTP configuration.\n\nSMTP Settings:\n" +
                    "Host: " + host + "\nPort: " + port + "\nAuth: " + enableAuth + "\nTLS: " + enableTls +
                    "\n\nIf you received this, your email configuration is working correctly!\n\nBest regards,\nVantage Admin");
            mailSender.send(message);
            return success("Test email sent successfully to " + to + "! Check your inbox.");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("STARTTLS")) {
                return error("STARTTLS error: Please enable TLS/SSL and use port 587. For Gmail, use App Password.");
            } else if (errorMsg.contains("Authentication")) {
                return error("Authentication failed: Use Gmail App Password, not regular password.");
            }
            return error("Failed to send test email: " + errorMsg);
        }
    }
    @Tag(name = "System - Configuration")
    @GetMapping("/system/config/key/{configKey}")
    @Operation(summary = "Get config by key", description = "Returns a configuration entry by its key")
    public AjaxResult getConfigByKey(@Parameter(description = "Configuration key") @PathVariable String configKey) {
        return systemConfigService.findByConfigKey(configKey)
                .map(this::success)
                .orElseGet(() -> error("Config not found"));
    }
    @Tag(name = "System - Configuration")
    @PutMapping("/system/config")
    @Operation(summary = "Update configuration", description = "Updates a configuration entry by key")
    @ApiResponse(responseCode = "200", description = "Configuration updated")
    public AjaxResult updateConfig(@RequestBody ConfigDTO config) {
        var existing = systemConfigService.findByConfigKey(config.getConfigKey()).orElse(null);
        if (existing == null) return error("Config not found");
        existing.setConfigValue(config.getConfigValue());
        existing.setUpdateBy("admin");
        systemConfigService.updateConfig(existing);
        return success("Configuration updated");
    }
    @Tag(name = "System - Configuration")
    @DeleteMapping("/system/config/{configId}")
    @Operation(summary = "Delete configuration", description = "Deletes a configuration entry by ID")
    @ApiResponse(responseCode = "200", description = "Configuration deleted")
    public AjaxResult removeConfig(@Parameter(description = "Configuration ID") @PathVariable Long configId) {
        if (systemConfigService.findById(configId).isEmpty()) {
            return error("Config not found");
        }
        systemConfigService.deleteConfigByIds(new Long[]{configId});
        return success("Config deleted");
    }

    // ========== System: OperLog ==========

    @Tag(name = "System - Operation Logs", description = "Operation audit log management")
    @GetMapping("/system/operlog/list")
    @Operation(summary = "List operation logs", description = "Returns paginated operation logs with filters")
    @ApiResponse(responseCode = "200", description = "Operation logs retrieved")
    public AjaxResult listOperLogs(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "Module title filter") @RequestParam(required = false) String title,
            @Parameter(description = "Operator name filter") @RequestParam(required = false) String operName,
            @Parameter(description = "Business type filter") @RequestParam(required = false) Integer businessType,
            @Parameter(description = "Status filter") @RequestParam(required = false) Integer status) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize);
        var page = systemOperLogService.findByConditionPaginated(title, operName, businessType, status, pageable);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        return success(result);
    }
    @Tag(name = "System - Operation Logs")
    @DeleteMapping("/system/operlog")
    @Operation(summary = "Delete operation logs", description = "Deletes operation logs by IDs")
    @ApiResponse(responseCode = "200", description = "Logs deleted")
    public AjaxResult removeOperLogs(@RequestBody Long[] operIds) {
        systemOperLogService.deleteByIds(operIds);
        return success();
    }
    @Tag(name = "System - Operation Logs")
    @DeleteMapping("/system/operlog/clean")
    @Operation(summary = "Clean all operation logs", description = "Removes all operation logs")
    @ApiResponse(responseCode = "200", description = "All logs cleaned")
    public AjaxResult cleanOperLogs() {
        systemOperLogService.cleanLogs();
        return success();
    }

    // ========== System: Logininfor ==========

    @Tag(name = "System - Login Info", description = "Login audit information management")
    @GetMapping("/system/logininfor/list")
    @Operation(summary = "List login information", description = "Returns paginated login records with filters")
    @ApiResponse(responseCode = "200", description = "Login records retrieved")
    public AjaxResult listLoginInfos(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "Username filter") @RequestParam(required = false) String loginName,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "IP address filter") @RequestParam(required = false) String ipaddr) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize);
        var page = systemLogininforService.findByConditionPaginated(loginName, status, ipaddr, pageable);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        return success(result);
    }
    @Tag(name = "System - Login Info")
    @DeleteMapping("/system/logininfor")
    @Operation(summary = "Delete login records", description = "Deletes login records by IDs")
    @ApiResponse(responseCode = "200", description = "Records deleted")
    public AjaxResult removeLoginInfos(@RequestBody Long[] infoIds) {
        systemLogininforService.deleteByIds(infoIds);
        return success();
    }
    @Tag(name = "System - Login Info")
    @DeleteMapping("/system/logininfor/clean")
    @Operation(summary = "Clean all login records", description = "Removes all login records")
    @ApiResponse(responseCode = "200", description = "All records cleaned")
    public AjaxResult cleanLoginInfos() {
        systemLogininforService.cleanLogs();
        return success();
    }

    // ========== System: Notice ==========

    @Tag(name = "System - Notices", description = "System notice announcements")
    @GetMapping("/system/notice/list")
    @Operation(summary = "List active notices", description = "Returns all active system notices")
    @ApiResponse(responseCode = "200", description = "Notices retrieved")
    public AjaxResult listNotices() {
        return success(systemNoticeService.findActiveNotices());
    }
    @Tag(name = "System - Notices")
    @GetMapping("/system/notice/{noticeId}")
    @Operation(summary = "Get notice by ID", description = "Returns a specific notice")
    public AjaxResult getNotice(@Parameter(description = "Notice ID") @PathVariable Integer noticeId) {
        return systemNoticeService.findById(noticeId.longValue())
                .map(this::success)
                .orElseGet(() -> error("Notice not found"));
    }
    @Tag(name = "System - Notices")
    @PostMapping("/system/notice")
    @Operation(summary = "Create notice", description = "Creates a new system notice")
    @ApiResponse(responseCode = "200", description = "Notice created")
    public AjaxResult addNotice(@RequestBody NoticeDTO notice) {
        notice.setStatus(notice.getStatus() != null ? notice.getStatus() : "0");
        notice.setCreateBy("admin");
        notice.setCreateTime(java.time.LocalDateTime.now());
        systemNoticeService.createNotice(notice);
        return success("Notice added successfully");
    }
    @Tag(name = "System - Notices")
    @PutMapping("/system/notice")
    @Operation(summary = "Update notice", description = "Updates an existing notice")
    @ApiResponse(responseCode = "200", description = "Notice updated")
    public AjaxResult updateNotice(@RequestBody NoticeDTO notice) {
        if (systemNoticeService.findById(notice.getNoticeId()).isEmpty()) {
            return error("Notice not found");
        }
        notice.setUpdateBy("admin");
        notice.setUpdateTime(java.time.LocalDateTime.now());
        systemNoticeService.updateNotice(notice);
        return success("Notice updated successfully");
    }
    @Tag(name = "System - Notices")
    @DeleteMapping("/system/notice/{noticeId}")
    @Operation(summary = "Delete notice", description = "Deletes a notice by ID")
    @ApiResponse(responseCode = "200", description = "Notice deleted")
    public AjaxResult removeNotice(@Parameter(description = "Notice ID") @PathVariable Integer noticeId) {
        if (systemNoticeService.findById(noticeId.longValue()).isEmpty()) {
            return error("Notice not found");
        }
        systemNoticeService.deleteNoticeByIds(new Long[]{noticeId.longValue()});
        return success("Notice deleted successfully");
    }

    // ========== System: Notifications ==========

    @Tag(name = "System - Notifications", description = "In-app notification management")
    @GetMapping("/system/notifications/list")
    @Operation(summary = "List notifications", description = "Returns paginated user notifications with unread count")
    @ApiResponse(responseCode = "200", description = "Notifications retrieved")
    public AjaxResult listNotifications(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = getCurrentUserId();
        if (userId == null) return error(401, "Not authenticated");
        var page = systemNotificationService.getNotifications(userId, pageNum, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("unreadCount", systemNotificationService.getUnreadCount(userId));
        return success(result);
    }
    @Tag(name = "System - Notifications")
    @GetMapping("/system/notifications/unread")
    @Operation(summary = "List unread notifications", description = "Returns paginated unread notifications")
    @ApiResponse(responseCode = "200", description = "Unread notifications retrieved")
    public AjaxResult unreadNotifications(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = getCurrentUserId();
        if (userId == null) return error(401, "Not authenticated");
        var page = systemNotificationService.getUnreadNotifications(userId, pageNum, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        return success(result);
    }
    @Tag(name = "System - Notifications")
    @GetMapping("/system/notifications/unread-count")
    @Operation(summary = "Get unread count", description = "Returns the count of unread notifications")
    @ApiResponse(responseCode = "200", description = "Unread count retrieved")
    public AjaxResult unreadCount() {
        Long userId = getCurrentUserId();
        if (userId == null) return error(401, "Not authenticated");
        return success(Map.of("count", systemNotificationService.getUnreadCount(userId)));
    }
    @Tag(name = "System - Notifications")
    @PutMapping("/system/notifications/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a single notification as read")
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    public AjaxResult markAsRead(@Parameter(description = "Notification ID") @PathVariable Long notificationId) {
        systemNotificationService.markAsRead(notificationId);
        return success();
    }
    @Tag(name = "System - Notifications")
    @PutMapping("/system/notifications/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all user notifications as read")
    @ApiResponse(responseCode = "200", description = "All notifications marked as read")
    public AjaxResult markAllAsRead() {
        Long userId = getCurrentUserId();
        if (userId == null) return error(401, "Not authenticated");
        systemNotificationService.markAllAsRead(userId);
        return success();
    }
    @Tag(name = "System - Notifications")
    @GetMapping("/system/notifications/statistics")
    @Operation(summary = "Get notification statistics", description = "Returns notification statistics for the user")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    public AjaxResult notificationStatistics() {
        Long userId = getCurrentUserId();
        if (userId == null) return error(401, "Not authenticated");
        return success(systemNotificationService.getStatistics(userId));
    }
    @Tag(name = "System - Notifications")
    @PostMapping("/system/notifications/test")
    @Operation(summary = "Send test notification", description = "Sends a test in-app notification")
    @ApiResponse(responseCode = "200", description = "Test notification sent")
    public AjaxResult sendTestNotification(@RequestBody Map<String, String> params) {
        Long userId = getCurrentUserId();
        if (userId == null) return error(401, "Not authenticated");
        String title = params.get("title");
        String content = params.get("content");
        String type = params.getOrDefault("type", "INFO");
        systemNotificationService.sendInAppNotification(userId, title, content, type);
        return success("Notification sent");
    }

    // ========== System: Datasource ==========

    @Tag(name = "System - Datasources", description = "Database datasource management and connection testing")
    @GetMapping("/system/datasource/list")
    @Operation(summary = "List datasources", description = "Returns all configured datasources")
    @ApiResponse(responseCode = "200", description = "Datasources retrieved")
    public AjaxResult listDatasources() {
        return success(systemDatasourceService.findAll());
    }
    @Tag(name = "System - Datasources")
    @GetMapping("/system/datasource/{datasourceId}")
    @Operation(summary = "Get datasource by ID", description = "Returns a specific datasource configuration")
    public AjaxResult getDatasource(@Parameter(description = "Datasource ID") @PathVariable Long datasourceId) {
        return systemDatasourceService.findById(datasourceId)
                .map(this::success)
                .orElseGet(() -> error("Datasource not found"));
    }
    @Tag(name = "System - Datasources")
    @PostMapping("/system/datasource")
    @Operation(summary = "Create datasource", description = "Adds a new database datasource")
    @ApiResponse(responseCode = "200", description = "Datasource created")
    public AjaxResult addDatasource(@RequestBody DatasourceDTO datasource) {
        if (systemDatasourceService.existsByDatasourceKey(datasource.getDatasourceKey())) {
            return error("Datasource key already exists");
        }
        if (datasource.getDriverClass() == null || datasource.getDriverClass().isEmpty()) {
            datasource.setDriverClass(systemDatasourceService.getDriverClass(datasource.getDbType()));
        }
        systemDatasourceService.save(datasource);
        return success("Datasource added successfully");
    }
    @Tag(name = "System - Datasources")
    @PutMapping("/system/datasource")
    @Operation(summary = "Update datasource", description = "Updates an existing datasource")
    @ApiResponse(responseCode = "200", description = "Datasource updated")
    public AjaxResult updateDatasource(@RequestBody DatasourceDTO datasource) {
        if (systemDatasourceService.findById(datasource.getDatasourceId()).isEmpty()) {
            return error("Datasource not found");
        }
        systemDatasourceService.save(datasource);
        return success("Datasource updated successfully");
    }
    @Tag(name = "System - Datasources")
    @DeleteMapping("/system/datasource/{datasourceId}")
    @Operation(summary = "Delete datasource", description = "Deletes a datasource by ID")
    @ApiResponse(responseCode = "200", description = "Datasource deleted")
    public AjaxResult removeDatasource(@Parameter(description = "Datasource ID") @PathVariable Long datasourceId) {
        systemDatasourceService.deleteById(datasourceId);
        return success("Datasource deleted successfully");
    }
    @Tag(name = "System - Datasources")
    @PostMapping("/system/datasource/test")
    @Operation(summary = "Test datasource connection", description = "Tests connectivity to a database datasource")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connection test result"),
        @ApiResponse(responseCode = "500", description = "Connection failed")
    })
    public AjaxResult testDatasource(@RequestBody DatasourceDTO datasource) {
        try {
            boolean success = systemDatasourceService.testConnection(datasource);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "Connection successful!" : "Connection failed");
            result.put("lastTestTime", datasource.getLastTestTime());
            result.put("lastTestStatus", datasource.getLastTestStatus());
            return success(result);
        } catch (Exception e) {
            return error("Connection test failed: " + e.getMessage());
        }
    }
    @Tag(name = "System - Datasources")
    @GetMapping("/system/datasource/driver/{dbType}")
    @Operation(summary = "Get driver info for database type", description = "Returns JDBC driver class and URL pattern for a database type")
    @ApiResponse(responseCode = "200", description = "Driver info retrieved")
    public AjaxResult getDriverClass(@Parameter(description = "Database type (mysql, postgresql, etc.)") @PathVariable String dbType) {
        Map<String, String> result = new HashMap<>();
        result.put("driverClass", systemDatasourceService.getDriverClass(dbType));
        result.put("urlPattern", systemDatasourceService.getDefaultUrlPattern(dbType));
        return success(result);
    }

    // ========== System: Email Config ==========

    @Tag(name = "System - Email Config", description = "SMTP email configuration management")
    @Tag(name = "System - Email Config")
    @GetMapping("/system/email-config")
    @Operation(summary = "Get email configuration", description = "Returns the current SMTP configuration")
    @ApiResponse(responseCode = "200", description = "Email config retrieved")
    public AjaxResult getEmailConfig() {
        return success(systemEmailConfigService.getConfig());
    }
    @Tag(name = "System - Email Config")
    @PostMapping("/system/email-config")
    @Operation(summary = "Save email configuration", description = "Saves SMTP configuration")
    @ApiResponse(responseCode = "200", description = "Email config saved")
    public AjaxResult saveEmailConfig(@RequestBody Map<String, Object> config) {
        return success(systemEmailConfigService.saveConfig(config));
    }
    @Tag(name = "System - Email Config")
    @PostMapping("/system/email-config/test")
    @Operation(summary = "Test email configuration", description = "Sends a test email using saved SMTP settings")
    @ApiResponse(responseCode = "200", description = "Test email sent or error message")
    public AjaxResult testEmailConfig(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        if (to == null || to.isEmpty()) return error("Recipient email is required");
        return success(systemEmailConfigService.sendTestEmail(to));
    }

    // ========== System: Cache ==========

    @Tag(name = "System - Cache", description = "Cache management and statistics")
    @Tag(name = "System - Cache")
    @GetMapping("/system/cache/list")
    @Operation(summary = "List all caches", description = "Returns all cache names and their statistics")
    @ApiResponse(responseCode = "200", description = "Cache list retrieved")
    public AjaxResult listCaches() {
        return success(systemCacheService.listCaches());
    }
    @Tag(name = "System - Cache")
    @PostMapping("/system/cache/clear/{cacheName}")
    @Operation(summary = "Clear specific cache", description = "Clears all entries in a named cache")
    @ApiResponse(responseCode = "200", description = "Cache cleared")
    public AjaxResult clearCache(@Parameter(description = "Cache name") @PathVariable String cacheName) {
        return success(systemCacheService.clearCache(cacheName));
    }
    @Tag(name = "System - Cache")
    @PostMapping("/system/cache/clear-all")
    @Operation(summary = "Clear all caches", description = "Clears all application caches")
    @ApiResponse(responseCode = "200", description = "All caches cleared")
    public AjaxResult clearAllCaches() {
        return success(systemCacheService.clearAllCaches());
    }
    @Tag(name = "System - Cache")
    @GetMapping("/system/cache/stats/{cacheName}")
    @Operation(summary = "Get cache statistics", description = "Returns statistics for a specific cache")
    @ApiResponse(responseCode = "200", description = "Cache statistics retrieved")
    public AjaxResult getCacheStats(@Parameter(description = "Cache name") @PathVariable String cacheName) {
        return success(systemCacheService.getCacheStats(cacheName));
    }

    // ========== System: Chat ==========

    @Tag(name = "AI Chat", description = "AI-powered chat assistant with SSE streaming and tool execution")
    @Tag(name = "AI Chat")
    @PostMapping("/chat")
    @Operation(summary = "Send chat message", description = "Sends a message to the AI assistant, optionally with tool results")
    @ApiResponse(responseCode = "200", description = "AI response returned")
    public AjaxResult chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> toolResults = (List<Map<String, Object>>) request.getOrDefault("toolResults", null);
        return success(systemChatService.chat(message, toolResults, getCurrentUsername()));
    }
    @Tag(name = "AI Chat")
    @GetMapping("/chat/status")
    @Operation(summary = "Get chat status", description = "Returns AI service status and knowledge base statistics")
    @ApiResponse(responseCode = "200", description = "Chat status retrieved")
    public AjaxResult chatStatus() {
        return success(systemChatService.chatStatus());
    }
    @Tag(name = "AI Chat")
    @PostMapping("/chat/knowledge/refresh")
    @Operation(summary = "Refresh knowledge base", description = "Reinitializes the AI knowledge base")
    @ApiResponse(responseCode = "200", description = "Knowledge base refreshed")
    public AjaxResult refreshKnowledge() {
        return success(systemChatService.refreshKnowledge());
    }
    @Tag(name = "AI Chat")
    @GetMapping("/chat/knowledge/stats")
    @Operation(summary = "Get knowledge stats", description = "Returns knowledge base statistics")
    @ApiResponse(responseCode = "200", description = "Knowledge stats retrieved")
    public AjaxResult knowledgeStats() {
        return success(systemChatService.knowledgeStats());
    }
    @Tag(name = "AI Chat")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream chat response", description = "Sends a message and receives streaming SSE response")
    @ApiResponse(responseCode = "200", description = "SSE stream started")
    public SseEmitter chatStream(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        return systemChatService.chatStream(message, getCurrentUsername());
    }
    @Tag(name = "AI Chat")
    @GetMapping("/chat/history")
    @Operation(summary = "Get conversation history", description = "Returns the user's conversation history")
    @ApiResponse(responseCode = "200", description = "Conversation history retrieved")
    public AjaxResult getConversationHistory() {
        return success(systemChatService.getConversationHistory(getCurrentUsername()));
    }
    @Tag(name = "AI Chat")
    @PostMapping("/chat/clear-memory")
    @Operation(summary = "Clear conversation memory", description = "Clears the user's AI conversation memory")
    @ApiResponse(responseCode = "200", description = "Memory cleared")
    public AjaxResult clearMemory() {
        return success(systemChatService.clearMemory(getCurrentUsername()));
    }
    @Tag(name = "AI Chat")
    @GetMapping("/chat/conversations")
    @Operation(summary = "List conversations", description = "Returns the user's conversation list")
    @ApiResponse(responseCode = "200", description = "Conversations retrieved")
    public AjaxResult listConversations() {
        return success(systemChatService.getConversationsList(getCurrentUsername()));
    }
    @Tag(name = "AI Chat")
    @GetMapping("/chat/conversations/{conversationId}")
    @Operation(summary = "Get conversation by ID", description = "Returns messages for a specific conversation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conversation history retrieved"),
        @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    public AjaxResult getConversationById(@Parameter(description = "Conversation ID") @PathVariable Long conversationId) {
        return success(systemChatService.getConversationHistoryById(conversationId));
    }
    @Tag(name = "AI Chat")
    @DeleteMapping("/chat/conversations/{conversationId}")
    @Operation(summary = "Delete conversation", description = "Deletes a specific conversation and its messages")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conversation deleted"),
        @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    public AjaxResult deleteConversation(@Parameter(description = "Conversation ID") @PathVariable Long conversationId) {
        return success(systemChatService.deleteConversation(conversationId, getCurrentUsername()));
    }
    @Tag(name = "AI Chat")
    @DeleteMapping("/chat/conversations")
    @Operation(summary = "Delete all conversations", description = "Deletes all conversations for the current user")
    @ApiResponse(responseCode = "200", description = "All conversations deleted")
    public AjaxResult deleteAllConversations() {
        return success(systemChatService.deleteAllConversations(getCurrentUsername()));
    }

    private String getCurrentUsername() {
        try {
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof com.pd.modules.system.security.LoginUser loginUser) {
                return loginUser.getUsername();
            }
        } catch (Exception ignored) {}
        return "anonymous";
    }

    private Long getCurrentUserId() {
        try {
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof com.pd.modules.system.security.LoginUser loginUser) {
                return loginUser.getUser().getUserId();
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ========== System: Report ==========

    @Tag(name = "System - Reports", description = "Report generation, scheduling, and templates")
    @Tag(name = "System - Reports")
    @GetMapping("/system/report/list")
    @Operation(summary = "List all reports", description = "Returns all saved reports")
    @ApiResponse(responseCode = "200", description = "Reports retrieved")
    public AjaxResult listReports() {
        return success(systemReportEntityService.findAllReports());
    }
    @Tag(name = "System - Reports")
    @GetMapping("/system/report/{reportId}")
    @Operation(summary = "Get report by ID", description = "Returns a specific report")
    public AjaxResult getReport(@Parameter(description = "Report ID") @PathVariable Long reportId) {
        return Optional.ofNullable(systemReportEntityService.findById(reportId))
                .map(this::success)
                .orElseGet(() -> error("Report not found"));
    }
    @Tag(name = "System - Reports")
    @PostMapping("/system/report")
    @Operation(summary = "Create report", description = "Creates a new report")
    @ApiResponse(responseCode = "200", description = "Report created")
    public AjaxResult addReport(@RequestBody Object report) {
        return success(systemReportEntityService.createReport(report));
    }
    @Tag(name = "System - Reports")
    @PutMapping("/system/report")
    @Operation(summary = "Update report", description = "Updates an existing report")
    @ApiResponse(responseCode = "200", description = "Report updated")
    public AjaxResult updateReport(@RequestBody Object report) {
        return success(systemReportEntityService.updateReport(report));
    }
    @Tag(name = "System - Reports")
    @DeleteMapping("/system/report/{reportId}")
    @Operation(summary = "Delete report", description = "Deletes a report by ID")
    @ApiResponse(responseCode = "200", description = "Report deleted")
    public AjaxResult removeReport(@Parameter(description = "Report ID") @PathVariable Long reportId) {
        return success(systemReportEntityService.deleteReport(reportId));
    }
    @Tag(name = "System - Reports")
    @PostMapping("/system/report/execute/{reportId}")
    @Operation(summary = "Execute report", description = "Executes a report with optional parameters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report executed"),
        @ApiResponse(responseCode = "500", description = "Execution failed")
    })
    public AjaxResult executeReport(@Parameter(description = "Report ID") @PathVariable Long reportId, @RequestBody(required = false) Map<String, Object> params) {
        try {
            String paramsJson = "{}";
            if (params != null && !params.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                paramsJson = mapper.writeValueAsString(params);
            }
            return success(systemReportEntityService.executeReport(reportId, paramsJson));
        } catch (Exception e) {
            return error("Failed to execute report: " + e.getMessage());
        }
    }
    @Tag(name = "System - Reports")
    @GetMapping("/system/report/templates")
    @Operation(summary = "List report templates", description = "Returns all report templates")
    @ApiResponse(responseCode = "200", description = "Templates retrieved")
    public AjaxResult getReportTemplates() {
        return success(systemReportEntityService.getTemplates());
    }
    @Tag(name = "System - Reports")
    @GetMapping("/system/report/template/{templateId}")
    @Operation(summary = "Get report template", description = "Returns a specific report template")
    public AjaxResult getReportTemplate(@Parameter(description = "Template ID") @PathVariable Long templateId) {
        return Optional.ofNullable(systemReportEntityService.getTemplate(templateId))
                .map(this::success)
                .orElseGet(() -> error("Template not found"));
    }
    @Tag(name = "System - Reports")
    @PostMapping("/system/report/from-template")
    @Operation(summary = "Create report from template", description = "Creates a new report from an existing template")
    @ApiResponse(responseCode = "200", description = "Report created from template")
    public AjaxResult createReportFromTemplate(@RequestBody Map<String, Object> request) {
        return success(systemReportEntityService.createReportFromTemplate(request));
    }
    @Tag(name = "System - Reports")
    @PostMapping("/system/report/schedule/{templateId}")
    @Operation(summary = "Schedule report", description = "Schedules a report template for recurring execution")
    @ApiResponse(responseCode = "200", description = "Report scheduled")
    public AjaxResult scheduleReport(@Parameter(description = "Template ID") @PathVariable Long templateId, @RequestBody Map<String, Object> config) {
        return success(systemReportEntityService.scheduleReport(templateId, config));
    }
    @Tag(name = "System - Reports")
    @DeleteMapping("/system/report/unschedule/{reportId}")
    @Operation(summary = "Unschedule report", description = "Removes the schedule from a report")
    @ApiResponse(responseCode = "200", description = "Report unscheduled")
    public AjaxResult unscheduleReport(@Parameter(description = "Report ID") @PathVariable Long reportId) {
        return success(systemReportEntityService.unscheduleReport(reportId));
    }

    // ========== System: Report Designer ==========

    @Tag(name = "System - Report Designer", description = "Advanced report designer with template versioning")
    @Tag(name = "System - Report Designer")
    @GetMapping("/system/report-designer/templates")
    @Operation(summary = "List report designer templates", description = "Returns report designer templates")
    public AjaxResult listReportDesignerTemplates(@Parameter(description = "Include all versions") @RequestParam(required = false, defaultValue = "false") Boolean allVersions) {
        return success(systemReportEntityService.listReportDesignerTemplates(allVersions));
    }
    @Tag(name = "System - Report Designer")
    @GetMapping("/system/report-designer/templates/{templateId}")
    @Operation(summary = "Get template by ID", description = "Returns a report designer template")
    public AjaxResult getReportDesignerTemplate(@Parameter(description = "Template ID") @PathVariable Long templateId) {
        return Optional.ofNullable(systemReportEntityService.getReportDesignerTemplate(templateId))
                .map(this::success)
                .orElseGet(() -> error("Template not found"));
    }
    @Tag(name = "System - Report Designer")
    @GetMapping("/system/report-designer/templates/key/{templateKey}")
    @Operation(summary = "Get template by key", description = "Returns a report designer template by key")
    public AjaxResult getReportDesignerTemplateByKey(@Parameter(description = "Template key") @PathVariable String templateKey) {
        return Optional.ofNullable(systemReportEntityService.getReportDesignerTemplateByKey(templateKey))
                .map(this::success)
                .orElseGet(() -> error("Template not found"));
    }
    @Tag(name = "System - Report Designer")
    @PostMapping("/system/report-designer/templates")
    @Operation(summary = "Create template", description = "Creates a new report designer template")
    public AjaxResult addReportDesignerTemplate(@RequestBody Map<String, Object> template) {
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            SysReportTemplate sysTemplate = mapper.convertValue(template, SysReportTemplate.class);
            return success(systemReportEntityService.addReportDesignerTemplate(sysTemplate));
        } catch (Exception e) {
            return error("Failed to create template: " + e.getMessage());
        }
    }
    @Tag(name = "System - Report Designer")
    @PutMapping("/system/report-designer/templates")
    @Operation(summary = "Update template", description = "Updates a report designer template")
    public AjaxResult updateReportDesignerTemplate(@RequestBody Map<String, Object> template) {
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            SysReportTemplate sysTemplate = mapper.convertValue(template, SysReportTemplate.class);
            return success(systemReportEntityService.updateReportDesignerTemplate(sysTemplate));
        } catch (Exception e) {
            return error("Failed to update template: " + e.getMessage());
        }
    }
    @Tag(name = "System - Report Designer")
    @DeleteMapping("/system/report-designer/templates/{templateId}")
    @Operation(summary = "Delete template", description = "Deletes a report designer template")
    public AjaxResult deleteReportDesignerTemplate(@Parameter(description = "Template ID") @PathVariable Long templateId) {
        return success(systemReportEntityService.deleteReportDesignerTemplate(templateId));
    }
    @Tag(name = "System - Report Designer")
    @GetMapping("/system/report-designer/templates/{templateKey}/versions")
    @Operation(summary = "Get template versions", description = "Returns all versions of a template")
    public AjaxResult getReportDesignerTemplateVersions(@Parameter(description = "Template key") @PathVariable String templateKey) {
        return success(systemReportEntityService.getReportDesignerTemplateVersions(templateKey));
    }
    @Tag(name = "System - Report Designer")
    @GetMapping("/system/report-designer/templates/active-versions")
    @Operation(summary = "Get active versions", description = "Returns currently active template versions")
    public AjaxResult getReportDesignerActiveVersions() {
        return success(systemReportEntityService.getReportDesignerActiveVersions());
    }

    @Tag(name = "System - Report Designer")

    @GetMapping("/system/report-designer/active-templates")
    @Operation(summary = "Get active templates", description = "Returns all active templates")
    public AjaxResult getReportDesignerActiveTemplates() {
        return success(systemReportEntityService.getReportDesignerActiveTemplates());
    }

    @Tag(name = "System - Report Designer")

    @PutMapping("/system/report-designer/templates/{templateId}/archive")
    @Operation(summary = "Archive template", description = "Archives a template version")
    public AjaxResult archiveReportDesignerTemplate(@Parameter(description = "Template ID") @PathVariable Long templateId) {
        return success(systemReportEntityService.archiveReportDesignerTemplate(templateId));
    }
    @Tag(name = "System - Report Designer")
    @PutMapping("/system/report-designer/templates/{templateId}/activate")
    @Operation(summary = "Activate template", description = "Activates a template version")
    public AjaxResult activateReportDesignerTemplate(@Parameter(description = "Template ID") @PathVariable Long templateId) {
        return success(systemReportEntityService.activateReportDesignerTemplate(templateId));
    }
    @Tag(name = "System - Report Designer")
    @GetMapping("/system/report-designer/datasource/{datasourceKey}/tables")
    @Operation(summary = "Get datasource tables", description = "Returns tables for a datasource")
    public AjaxResult getDatasourceTables(@Parameter(description = "Datasource key") @PathVariable String datasourceKey) {
        return success(systemReportEntityService.getDatasourceTables(datasourceKey));
    }
    @Tag(name = "System - Report Designer")
    @PostMapping("/system/report-designer/execute/{templateId}")
    @Operation(summary = "Execute template", description = "Executes a report designer template")
    public AjaxResult executeReportDesignerTemplate(@Parameter(description = "Template ID") @PathVariable Long templateId, @RequestBody(required = false) Map<String, Object> params) {
        try {
            return success(systemReportEntityService.executeReportDesignerTemplate(templateId, params));
        } catch (Exception e) {
            return error("Failed to execute report: " + e.getMessage());
        }
    }
    @Tag(name = "System - Report Designer")
    @PostMapping("/system/report-designer/preview")
    @Operation(summary = "Preview report", description = "Previews a report with given template")
    public AjaxResult previewReport(@RequestBody Map<String, Object> request, @RequestParam(required = false) String params) {
        try {
            Long templateId;
            if (request.containsKey("templateId")) {
                templateId = Long.valueOf(request.get("templateId").toString());
            } else if (request.containsKey("template")) {
                Map<String, Object> tpl = (Map<String, Object>) request.get("template");
                templateId = Long.valueOf(tpl.get("templateId").toString());
            } else {
                return error("templateId is required");
            }
            String paramsJson = params != null ? params : "{}";
            if (request.containsKey("params") && params == null) {
                paramsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request.get("params"));
            }

            List<Map<String, Object>> results = reportDesignerService.executeTemplate(templateId, paramsJson);

            String sqlContent = null;
            if (request.containsKey("sqlContent")) {
                sqlContent = request.get("sqlContent").toString();
            }

            Map<String, Object> previewResult = new HashMap<>();
            previewResult.put("data", results);
            previewResult.put("count", results.size());
            previewResult.put("sql", sqlContent);
            return success(previewResult);
        } catch (Exception e) {
            return error("Preview failed: " + e.getMessage());
        }
    }

    // ========== Quartz: Job ==========

    @Tag(name = "Quartz - Job Management", description = "Scheduled job CRUD and execution control")
    @GetMapping("/system/job/list")
    @Operation(summary = "List all jobs", description = "Returns paginated list of scheduled jobs with optional filters")
    @ApiResponse(responseCode = "200", description = "Jobs retrieved")
    public AjaxResult listJobs(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "Filter by job name") @RequestParam(required = false) String jobName,
            @Parameter(description = "Filter by job group") @RequestParam(required = false) String jobGroup,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize);
        var page = quartzJobService.searchJobs(jobName, jobGroup, status, pageable);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        return success(result);
    }

    @Tag(name = "Quartz - Job Management")

    @GetMapping("/system/job/{jobId}")
    @Operation(summary = "Get job by ID", description = "Returns a specific scheduled job")
    public AjaxResult getJob(@Parameter(description = "Job ID") @PathVariable Long jobId) {
        return quartzJobService.findById(jobId)
                .map(this::success)
                .orElseGet(() -> error("Job not found"));
    }

    @Tag(name = "Quartz - Job Management")

    @PostMapping("/system/job")
    @Operation(summary = "Create job", description = "Creates a new scheduled job")
    @ApiResponse(responseCode = "200", description = "Job created")
    public AjaxResult addJob(@RequestBody JobDTO job) {
        quartzJobService.createJob(job);
        return success("Job added successfully");
    }

    @Tag(name = "Quartz - Job Management")

    @PutMapping("/system/job")
    @Operation(summary = "Update job", description = "Updates an existing scheduled job")
    @ApiResponse(responseCode = "200", description = "Job updated")
    public AjaxResult updateJob(@RequestBody JobDTO job) {
        quartzJobService.updateJob(job);
        return success("Job updated successfully");
    }

    @Tag(name = "Quartz - Job Management")

    @DeleteMapping("/system/job/{jobId}")
    @Operation(summary = "Delete job", description = "Deletes a scheduled job")
    @ApiResponse(responseCode = "200", description = "Job deleted")
    public AjaxResult removeJob(@Parameter(description = "Job ID") @PathVariable Long jobId) {
        quartzJobService.deleteJob(jobId);
        return success("Job deleted successfully");
    }

    @Tag(name = "Quartz - Job Management")

    @DeleteMapping("/system/job/batch")
    @Operation(summary = "Batch delete jobs", description = "Deletes multiple jobs by IDs")
    @ApiResponse(responseCode = "200", description = "Jobs deleted")
    public AjaxResult batchRemoveJobs(@RequestBody Long[] ids) {
        for (Long id : ids) {
            quartzJobService.deleteJob(id);
        }
        return success("Deleted " + ids.length + " job(s)");
    }

    @Tag(name = "Quartz - Job Management")

    @PutMapping("/system/job/changeStatus")
    @Operation(summary = "Change job status", description = "Updates a job's enabled/disabled status")
    @ApiResponse(responseCode = "200", description = "Status updated")
    public AjaxResult changeJobStatus(@RequestBody JobDTO job) {
        Long jobId = job.getJobId();
        String status = job.getStatus();
        if ("1".equals(status)) {
            quartzJobService.pauseJob(jobId);
        } else if ("0".equals(status)) {
            quartzJobService.resumeJob(jobId);
        } else {
            return error("Invalid status value");
        }
        return success("Job status updated successfully");
    }

    @Tag(name = "Quartz - Job Management")

    @PostMapping("/system/job/run")
    @Operation(summary = "Run job immediately", description = "Triggers immediate execution of a job")
    @ApiResponse(responseCode = "200", description = "Job triggered")
    public AjaxResult runJob(@RequestBody Map<String, Object> request) {
        Long jobId = request.get("jobId") != null ? ((Number) request.get("jobId")).longValue() : null;
        if (jobId == null) return error("Job ID is required");
        quartzJobService.runJob(jobId);
        return success();
    }

    @Tag(name = "Quartz - Job Management")

    @PutMapping("/system/job/pause")
    @Operation(summary = "Pause job", description = "Pauses a scheduled job")
    @ApiResponse(responseCode = "200", description = "Job paused")
    public AjaxResult pauseJob(@RequestBody JobDTO job) {
        quartzJobService.pauseJob(job.getJobId());
        return success("Job paused successfully");
    }

    @Tag(name = "Quartz - Job Management")

    @PutMapping("/system/job/resume")
    @Operation(summary = "Resume job", description = "Resumes a paused scheduled job")
    @ApiResponse(responseCode = "200", description = "Job resumed")
    public AjaxResult resumeJob(@RequestBody JobDTO job) {
        quartzJobService.resumeJob(job.getJobId());
        return success("Job resumed successfully");
    }

    @Tag(name = "Quartz - Job Management")

    @PutMapping("/system/job/batch/pause")
    @Operation(summary = "Batch pause jobs", description = "Pauses multiple scheduled jobs by IDs")
    @ApiResponse(responseCode = "200", description = "Jobs paused")
    public AjaxResult batchPauseJobs(@RequestBody Long[] ids) {
        for (Long id : ids) {
            quartzJobService.pauseJob(id);
        }
        return success("Paused " + ids.length + " job(s)");
    }

    @Tag(name = "Quartz - Job Management")

    @PutMapping("/system/job/batch/resume")
    @Operation(summary = "Batch resume jobs", description = "Resumes multiple paused scheduled jobs by IDs")
    @ApiResponse(responseCode = "200", description = "Jobs resumed")
    public AjaxResult batchResumeJobs(@RequestBody Long[] ids) {
        for (Long id : ids) {
            quartzJobService.resumeJob(id);
        }
        return success("Resumed " + ids.length + " job(s)");
    }

    @Tag(name = "Quartz - Job Management")

    @PostMapping("/system/job/batch/run")
    @Operation(summary = "Batch run jobs", description = "Executes multiple jobs immediately by IDs")
    @ApiResponse(responseCode = "200", description = "Jobs executed")
    public AjaxResult batchRunJobs(@RequestBody Long[] ids) {
        int successCount = 0;
        for (Long id : ids) {
            try {
                quartzJobService.runJob(id);
                successCount++;
            } catch (Exception ignored) {}
        }
        return success("Executed " + successCount + "/" + ids.length + " job(s)");
    }

    @Tag(name = "Quartz - Job Management")

    @GetMapping("/system/job/export")
    @Operation(summary = "Export jobs", description = "Exports selected or all jobs as JSON")
    @ApiResponse(responseCode = "200", description = "Jobs exported")
    public AjaxResult exportJobs(@RequestParam(required = false) Long[] ids) {
        List<JobDTO> jobs;
        if (ids != null && ids.length > 0) {
            jobs = new ArrayList<>();
            for (Long id : ids) {
                quartzJobService.findById(id).ifPresent(jobs::add);
            }
        } else {
            jobs = quartzJobService.findAll();
        }
        return success(jobs);
    }

    @Tag(name = "Quartz - Job Management")

    @PostMapping("/system/job/import")
    @Operation(summary = "Import jobs", description = "Imports jobs from JSON data")
    @ApiResponse(responseCode = "200", description = "Jobs imported")
    public AjaxResult importJobs(@RequestBody List<JobDTO> jobs) {
        int successCount = 0;
        for (JobDTO job : jobs) {
            try {
                job.setJobId(null);
                quartzJobService.createJob(job);
                successCount++;
            } catch (Exception ignored) {}
        }
        return success("Imported " + successCount + "/" + jobs.size() + " job(s)");
    }

    @Tag(name = "Quartz - Job Management")

    @GetMapping("/system/job/groups")
    @Operation(summary = "Get job groups", description = "Returns list of distinct job group names")
    @ApiResponse(responseCode = "200", description = "Job groups retrieved")
    public AjaxResult getJobGroups() {
        List<String> groups = quartzJobService.findAll().stream()
                .map(JobDTO::getJobGroup)
                .distinct()
                .toList();
        return success(groups);
    }

    // ========== Quartz: Job Log ==========

    @Tag(name = "Quartz - Job Logs", description = "Job execution log management")
    @GetMapping("/system/job-log/list")
    @Operation(summary = "List job logs", description = "Returns paginated job execution logs with filters")
    @ApiResponse(responseCode = "200", description = "Job logs retrieved")
    public AjaxResult listJobLogs(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "Job name filter") @RequestParam(required = false) String jobName,
            @Parameter(description = "Job group filter") @RequestParam(required = false) String jobGroup,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize);
        var page = quartzJobLogService.findByConditionPaginated(jobName, jobGroup, status, null, null, pageable);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        return success(result);
    }

    @Tag(name = "Quartz - Job Logs")

    @GetMapping("/system/job-log/job/{jobId}")
    @Operation(summary = "Get job logs by job ID", description = "Returns execution logs for a specific job")
    @ApiResponse(responseCode = "200", description = "Job logs retrieved")
    public AjaxResult getJobLogByJobId(@Parameter(description = "Job ID") @PathVariable Long jobId) {
        var jobOpt = quartzJobLogService.findById(jobId);
        return jobOpt.map(j -> success(List.of(j))).orElseGet(() -> error("Job log not found"));
    }

    @Tag(name = "Quartz - Job Logs")

    @GetMapping("/system/job-log/{logId}")
    @Operation(summary = "Get job log by ID", description = "Returns a specific execution log entry")
    @ApiResponse(responseCode = "200", description = "Job log retrieved")
    public AjaxResult getJobLogById(@Parameter(description = "Log ID") @PathVariable Long logId) {
        return quartzJobLogService.findById(logId)
                .map(this::success)
                .orElseGet(() -> error("Job log not found"));
    }

    @Tag(name = "Quartz - Job Logs")

    @GetMapping("/system/job-log/failed/recent")
    @Operation(summary = "Get recent failed logs", description = "Returns recently failed job execution logs")
    @ApiResponse(responseCode = "200", description = "Failed logs retrieved")
    public AjaxResult getRecentFailedLogs(@Parameter(description = "Limit count") @RequestParam(defaultValue = "10") int limit) {
        var logs = quartzJobLogService.findAll().stream()
                .filter(l -> "1".equals(l.getStatus()))
                .limit(limit)
                .toList();
        return success(logs);
    }

    @Tag(name = "Quartz - Job Logs")

    @DeleteMapping("/system/job-log/{logId}")
    @Operation(summary = "Delete job log", description = "Deletes a specific execution log entry")
    @ApiResponse(responseCode = "200", description = "Log deleted")
    public AjaxResult removeJobLog(@Parameter(description = "Log ID") @PathVariable Long logId) {
        quartzJobLogService.deleteByIds(new Long[]{logId});
        return success("Job log deleted successfully");
    }

    @Tag(name = "Quartz - Job Logs")

    @DeleteMapping("/system/job-log/batch")
    @Operation(summary = "Batch delete job logs", description = "Deletes multiple execution log entries by IDs")
    @ApiResponse(responseCode = "200", description = "Logs deleted")
    public AjaxResult batchRemoveJobLogs(@Parameter(description = "Log IDs") @RequestBody Long[] ids) {
        quartzJobLogService.deleteByIds(ids);
        return success("Deleted " + ids.length + " job log(s)");
    }

    @Tag(name = "Quartz - Job Logs")

    @DeleteMapping("/system/job-log/clean")
    @Operation(summary = "Clean all job logs", description = "Removes all job execution logs")
    @ApiResponse(responseCode = "200", description = "All logs cleared")
    public AjaxResult cleanJobLogs() {
        quartzJobLogService.cleanLogs();
        return success("All job logs cleared");
    }

    // ========== Quartz: Job Template ==========

    @Tag(name = "Quartz - Job Templates", description = "Job template management")
    @GetMapping("/system/job-template/list")
    @Operation(summary = "List job templates", description = "Returns all available job templates")
    @ApiResponse(responseCode = "200", description = "Templates retrieved")
    public AjaxResult listJobTemplates() {
        return success(quartzJobTemplateService.getTemplates());
    }

    @Tag(name = "Quartz - Job Templates")

    @GetMapping("/system/job-template/{name}")
    @Operation(summary = "Get job template by name", description = "Returns a specific job template")
    @ApiResponse(responseCode = "200", description = "Template retrieved")
    public AjaxResult getJobTemplate(@Parameter(description = "Template name") @PathVariable String name) {
        var template = quartzJobTemplateService.getTemplateByName(name);
        return template != null ? success(template) : error("Template not found");
    }

    @Tag(name = "Quartz - Job Templates")

    @PostMapping("/system/job-template/create/{templateName}")
    @Operation(summary = "Create job from template", description = "Creates a new scheduled job from a template")
    @ApiResponse(responseCode = "200", description = "Job created from template")
    public AjaxResult createJobFromTemplate(@Parameter(description = "Template name") @PathVariable String templateName, @Parameter(description = "Job name (optional)") @RequestParam(required = false) String jobName) {
        JobDTO job = quartzJobTemplateService.createJobFromTemplate(templateName, jobName);
        quartzJobService.createJob(job);
        return success("Job created from template: " + templateName);
    }

    // ========== Quartz: Email Template ==========

    @Tag(name = "Quartz - Email Templates", description = "Email notification template management")
    @GetMapping("/system/email-template/list")
    @Operation(summary = "List email templates", description = "Returns all email notification templates")
    @ApiResponse(responseCode = "200", description = "Templates retrieved")
    public AjaxResult listEmailTemplates() {
        return success(quartzEmailJobTemplateService.getAllTemplates());
    }

    @Tag(name = "Quartz - Email Templates")

    @GetMapping("/system/email-template/active")
    @Operation(summary = "Get active email templates", description = "Returns only active email templates")
    @ApiResponse(responseCode = "200", description = "Active templates retrieved")
    public AjaxResult getActiveEmailTemplates() {
        return success(quartzEmailJobTemplateService.getActiveTemplates());
    }

    @Tag(name = "Quartz - Email Templates")

    @GetMapping("/system/email-template/{templateId}")
    @Operation(summary = "Get email template by ID", description = "Returns a specific email template")
    @ApiResponse(responseCode = "200", description = "Template retrieved")
    public AjaxResult getEmailTemplate(@Parameter(description = "Template ID") @PathVariable Long templateId) {
        return quartzEmailJobTemplateService.getTemplateById(templateId)
                .map(this::success)
                .orElseGet(() -> error("Template not found"));
    }

    @Tag(name = "Quartz - Email Templates")

    @GetMapping("/system/email-template/type/{templateType}")
    @Operation(summary = "Get email template by type", description = "Returns email template by its type identifier")
    @ApiResponse(responseCode = "200", description = "Template retrieved")
    public AjaxResult getEmailTemplateByType(@Parameter(description = "Template type") @PathVariable String templateType) {
        return quartzEmailJobTemplateService.getTemplateByType(templateType)
                .map(this::success)
                .orElseGet(() -> error("Template not found for type: " + templateType));
    }

    @Tag(name = "Quartz - Email Templates")

    @PostMapping("/system/email-template")
    @Operation(summary = "Create email template", description = "Creates a new email notification template")
    @ApiResponse(responseCode = "200", description = "Template created")
    public AjaxResult addEmailTemplate(@RequestBody EmailTemplateDTO template) {
        quartzEmailJobTemplateService.saveTemplate(template);
        return success("Template created successfully");
    }

    @Tag(name = "Quartz - Email Templates")

    @PutMapping("/system/email-template")
    @Operation(summary = "Update email template", description = "Updates an existing email template")
    @ApiResponse(responseCode = "200", description = "Template updated")
    public AjaxResult updateEmailTemplate(@RequestBody EmailTemplateDTO template) {
        quartzEmailJobTemplateService.saveTemplate(template);
        return success("Template updated successfully");
    }

    @Tag(name = "Quartz - Email Templates")

    @DeleteMapping("/system/email-template/{templateId}")
    @Operation(summary = "Delete email template", description = "Deletes an email template by ID")
    @ApiResponse(responseCode = "200", description = "Template deleted")
    public AjaxResult removeEmailTemplate(@Parameter(description = "Template ID") @PathVariable Long templateId) {
        quartzEmailJobTemplateService.deleteTemplate(templateId);
        return success("Template deleted successfully");
    }

    @Tag(name = "Quartz - Email Templates")

    @PutMapping("/system/email-template/{templateId}/set-default")
    @Operation(summary = "Set default email template", description = "Sets an email template as default for a type")
    @ApiResponse(responseCode = "200", description = "Default template set")
    public AjaxResult setEmailTemplateAsDefault(@Parameter(description = "Template ID") @PathVariable Long templateId, @Parameter(description = "Template type") @RequestParam String templateType) {
        quartzEmailJobTemplateService.setTemplateAsDefault(templateId, templateType);
        return success("Template set as default");
    }

    @Tag(name = "Quartz - Email Templates")

    @PutMapping("/system/email-template/{templateId}/toggle-active")
    @Operation(summary = "Toggle email template active status", description = "Activates or deactivates an email template")
    @ApiResponse(responseCode = "200", description = "Template status updated")
    public AjaxResult toggleEmailTemplateActive(@Parameter(description = "Template ID") @PathVariable Long templateId) {
        EmailTemplateDTO template = quartzEmailJobTemplateService.getTemplateById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        template.setIsActive(!template.getIsActive());
        quartzEmailJobTemplateService.saveTemplate(template);
        return success("Template status updated");
    }

    @Tag(name = "Quartz - Email Templates")

    @PostMapping("/system/email-template/preview")
    @Operation(summary = "Preview email template", description = "Renders and previews an email template with data")
    @ApiResponse(responseCode = "200", description = "Template preview generated")
    public AjaxResult previewEmailTemplate(@RequestBody Map<String, Object> request) {
        try {
            String subject = (String) request.getOrDefault("emailSubject", "");
            String body = (String) request.getOrDefault("emailBody", "");
            String dataTablesJson = (String) request.get("dataTables");
            String params = (String) request.get("params");

            String renderedSubject = quartzEmailJobTemplateService.processTemplate(subject, null, null);
            String renderedBody = quartzEmailJobTemplateService.processTemplate(body, null, null);

            String dataTableHtml = "";
            if (dataTablesJson != null && !dataTablesJson.isEmpty()) {
                dataTableHtml = quartzEmailJobTemplateService.executeMultipleQueriesAndRenderTables(dataTablesJson, params);
                renderedBody = renderedBody.replace("${dataTable}", dataTableHtml);
            } else {
                renderedBody = renderedBody.replace("${dataTable}", "");
            }

            AjaxResult result = success();
            result.put("subject", renderedSubject);
            result.put("body", renderedBody);
            return result;
        } catch (Exception e) {
            return error("Preview failed: " + e.getMessage());
        }
    }

    // ========== Quartz: Job Group ==========

    @Tag(name = "Quartz - Job Groups", description = "Job group management and batch execution")
    @GetMapping("/system/job-group/list")
    @Operation(summary = "Get job groups summary", description = "Returns summary of all job groups with counts")
    @ApiResponse(responseCode = "200", description = "Job groups retrieved")
    public AjaxResult getJobGroupsSummary() {
        return success(quartzJobGroupService.getJobGroupSummary());
    }

    @Tag(name = "Quartz - Job Groups")

    @GetMapping("/system/job-group/{jobGroup}/jobs")
    @Operation(summary = "Get jobs in group", description = "Returns all jobs in a specific group")
    @ApiResponse(responseCode = "200", description = "Jobs retrieved")
    public AjaxResult getJobsInGroup(@Parameter(description = "Job group name") @PathVariable String jobGroup) {
        return success(quartzJobGroupService.getJobsInGroup(jobGroup));
    }

    @Tag(name = "Quartz - Job Groups")

    @PostMapping("/system/job-group/{jobGroup}/execute")
    @Operation(summary = "Execute job group", description = "Runs all jobs in a group sequentially")
    @ApiResponse(responseCode = "200", description = "Group executed")
    public AjaxResult executeJobGroup(@Parameter(description = "Job group name") @PathVariable String jobGroup) {
        List<Map<String, Object>> results = quartzJobGroupService.executeGroup(jobGroup);
        long successCount = results.stream().filter(r -> "SUCCESS".equals(r.get("status"))).count();
        long failedCount = results.stream().filter(r -> "FAILED".equals(r.get("status"))).count();
        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        if (failedCount > 0) {
            response.put("msg", "Group executed. Success: " + successCount + ", Failed: " + failedCount);
        } else {
            response.put("msg", "All jobs executed. Success: " + successCount);
        }
        return success(response);
    }

    // ========== Quartz: Job Dashboard ==========

    @Tag(name = "Quartz - Job Dashboard", description = "Job metrics, trends, and health monitoring")
    @GetMapping("/system/job-dashboard/metrics")
    @Operation(summary = "Get job metrics", description = "Returns dashboard metrics for all jobs")
    @ApiResponse(responseCode = "200", description = "Metrics retrieved")
    public AjaxResult getJobMetrics() {
        return success(quartzJobMetricsService.getDashboardMetrics());
    }

    @Tag(name = "Quartz - Job Dashboard")

    @GetMapping("/system/job-dashboard/trend")
    @Operation(summary = "Get job execution trend", description = "Returns execution trend data for charting")
    @ApiResponse(responseCode = "200", description = "Trend data retrieved")
    public AjaxResult getJobTrend(@Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        return success(quartzJobMetricsService.getExecutionTrend(days));
    }

    @Tag(name = "Quartz - Job Dashboard")

    @GetMapping("/system/job-dashboard/health")
    @Operation(summary = "Get job health status", description = "Returns health check status for all jobs")
    @ApiResponse(responseCode = "200", description = "Health status retrieved")
    public AjaxResult getJobHealth() {
        return success(quartzJobMetricsService.getJobHealth());
    }

    // ========== Quartz: Script Job ==========

    @Tag(name = "Quartz - Script Job", description = "Ad-hoc script execution")
    @PostMapping("/system/scriptJob/run")
    @Operation(summary = "Run script", description = "Executes an ad-hoc script (shell, python, etc.)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Script executed"),
        @ApiResponse(responseCode = "400", description = "Script content required")
    })
    public AjaxResult runScript(@RequestBody Map<String, String> params) {
        try {
            String scriptType = params.get("scriptType");
            String scriptContent = params.get("scriptContent");
            if (scriptContent == null || scriptContent.isEmpty()) {
                return error("Script content is required");
            }
            return success(quartzScriptJobService.runScript(scriptType, scriptContent));
        } catch (Exception e) {
            return error("Script execution failed: " + e.getMessage());
        }
    }

    // ========== Quartz: Job Webhook ==========

    @Tag(name = "Quartz - Job Webhook", description = "External webhook triggers for job execution")
    @PostMapping("/public/job/webhook/{jobId}")
    @Operation(summary = "Trigger job via webhook", description = "Executes a job using webhook URL and token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job triggered"),
        @ApiResponse(responseCode = "403", description = "Invalid webhook token")
    })
    public AjaxResult triggerJobWebhook(@Parameter(description = "Job ID") @PathVariable Long jobId, @Parameter(description = "Webhook token") @RequestParam String token) {
        return success(quartzJobWebhookService.triggerJobByWebhook(jobId, token));
    }

    // ========== Generator ==========

    @Tag(name = "Code Generator", description = "Database table code generation for CRUD operations")
    @GetMapping("/tool/gen/db/tables")
    @Operation(summary = "List database tables", description = "Returns all database tables available for code generation")
    @ApiResponse(responseCode = "200", description = "Tables retrieved")
    public AjaxResult listTables(@RequestParam(required = false, defaultValue = "master") String datasourceKey) {
        try {
            return success(genService.getDatabaseTables(datasourceKey));
        } catch (Exception e) {
            return error("Failed to load tables: " + e.getMessage());
        }
    }

    @Tag(name = "Code Generator")
    @PostMapping("/tool/gen/db/create-table")
    @Operation(summary = "Create a database table", description = "Creates a new table in the specified datasource from the UI definition")
    @ApiResponse(responseCode = "200", description = "Table created")
    public AjaxResult createTable(@RequestBody CreateTableRequest request) {
        try {
            genService.createTable(request);
            return success("Table '" + request.getTableName() + "' created successfully");
        } catch (Exception e) {
            return error("Failed to create table: " + e.getMessage());
        }
    }

    @Tag(name = "Code Generator")
    @GetMapping("/tool/gen/preview-code")
    @Operation(summary = "Preview generated code", description = "Returns generated code as text for a table")
    @ApiResponse(responseCode = "200", description = "Code preview")
    public AjaxResult previewCode(
            @RequestParam String tableName,
            @RequestParam(required = false) String tableComment,
            @RequestParam(required = false, defaultValue = "master") String datasourceKey,
            @RequestParam(required = false, defaultValue = "system") String moduleName,
            @RequestParam(required = false, defaultValue = "com.pd.modules") String packageName,
            @RequestParam(required = false, defaultValue = "admin") String author) {
        try {
            Map<String, String> files = genService.previewModule(tableName, tableComment, datasourceKey, moduleName, packageName, author);
            return success(files);
        } catch (Exception e) {
            return error("Preview failed: " + e.getMessage());
        }
    }

    @Tag(name = "Code Generator")
    @PostMapping("/tool/gen/db/clone-table")
    @Operation(summary = "Clone a database table", description = "Clones table schema (columns, types, comments) to a new table")
    @ApiResponse(responseCode = "200", description = "Table cloned")
    public AjaxResult cloneTable(@RequestBody CloneTableRequest request) {
        try {
            genService.cloneTable(request.getSourceTableName(), request.getNewTableName(), request.getNewTableComment(), request.getDatasourceKey());
            return success("Table '" + request.getNewTableName() + "' cloned from '" + request.getSourceTableName() + "' successfully");
        } catch (Exception e) {
            return error("Failed to clone table: " + e.getMessage());
        }
    }

    @Tag(name = "Code Generator")

    @GetMapping("/tool/gen/preview")
    @Operation(summary = "Preview generated code", description = "Returns preview of generated code for a table")
    @ApiResponse(responseCode = "200", description = "Code preview returned")
    public void preview(@Parameter(description = "Table ID") @RequestParam Long tableId, HttpServletResponse response) throws IOException {
        Map<String, String> code = generatorService.previewCode(tableId);
        response.setContentType("text/plain;charset=UTF-8");
        if (code != null && !code.isEmpty()) {
            response.getWriter().write(code.values().iterator().next());
        }
    }

    @Tag(name = "Code Generator")

    @PostMapping("/tool/gen/batch")
    @Operation(summary = "Batch generate code", description = "Imports tables and generates CRUD code for multiple tables")
    @ApiResponse(responseCode = "200", description = "Code generated")
    public AjaxResult batchGen(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<String> tables = (List<String>) params.get("tables");
            generatorService.importTables(tables);
            return success("Code generated successfully");
        } catch (Exception e) {
            return error("Failed to generate code: " + e.getMessage());
        }
    }

    @Tag(name = "Code Generator")

    @GetMapping("/tool/gen/download")
    @Operation(summary = "Download generated module", description = "Downloads generated module ZIP with entity, repository, service, controller, and menu SQL")
    @ApiResponse(responseCode = "200", description = "ZIP file downloaded")
    public void downloadModule(
            @RequestParam String tableName,
            @RequestParam(required = false) String tableComment,
            @RequestParam(required = false, defaultValue = "master") String datasourceKey,
            @RequestParam(required = false, defaultValue = "system") String moduleName,
            @RequestParam(required = false, defaultValue = "com.pd.modules") String packageName,
            @RequestParam(required = false, defaultValue = "admin") String author,
            HttpServletResponse response) throws IOException {
        try {
            genService.downloadModule(tableName, tableComment, datasourceKey, moduleName, packageName, author, response);
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Error: " + e.getMessage());
        }
    }

    // ========== SPA Forwarding ==========

    // Note: SPA forwarding is handled by a separate internal controller
}
