package com.pd.gateway;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.generator.api.GeneratorService;
import com.pd.modules.quartz.api.*;
import com.pd.modules.quartz.api.dto.EmailTemplateDTO;
import com.pd.modules.quartz.api.dto.JobDTO;
import com.pd.modules.quartz.api.dto.JobLogDTO;
import com.pd.modules.system.api.*;
import com.pd.modules.system.api.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

/**
 * Gateway Management Controller - sole external HTTP entry point.
 * All requests route through module API interfaces only.
 * No module controller is directly exposed.
 */
@RestController
@RequestMapping("/api")
public class GatewayManagement extends BaseController {

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
            QuartzJobService quartzJobService,
            QuartzJobLogService quartzJobLogService,
            QuartzJobTemplateService quartzJobTemplateService,
            QuartzEmailJobTemplateService quartzEmailJobTemplateService,
            QuartzJobGroupService quartzJobGroupService,
            QuartzJobMetricsService quartzJobMetricsService,
            QuartzScriptJobService quartzScriptJobService,
            QuartzJobWebhookService quartzJobWebhookService,
            GeneratorService generatorService) {
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
        this.quartzJobService = quartzJobService;
        this.quartzJobLogService = quartzJobLogService;
        this.quartzJobTemplateService = quartzJobTemplateService;
        this.quartzEmailJobTemplateService = quartzEmailJobTemplateService;
        this.quartzJobGroupService = quartzJobGroupService;
        this.quartzJobMetricsService = quartzJobMetricsService;
        this.quartzScriptJobService = quartzScriptJobService;
        this.quartzJobWebhookService = quartzJobWebhookService;
        this.generatorService = generatorService;
    }

    // ========== Auth ==========

    @GetMapping("/me")
    public AjaxResult me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return error("Not authenticated");
        }
        return success(systemAuthService.getCurrentUser());
    }

    @PostMapping("/logout")
    public AjaxResult logout() {
        return success(systemAuthService.logout());
    }

    // ========== System: User ==========

    @GetMapping("/system/user/list")
    public AjaxResult listUsers() {
        return success(systemUserService.findAllActive());
    }

    @GetMapping("/system/user/{userId}")
    public AjaxResult getUser(@PathVariable Long userId) {
        return systemUserService.findById(userId)
                .map(this::success)
                .orElseGet(() -> error("User not found"));
    }

    @PostMapping("/system/user")
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

    @PutMapping("/system/user")
    public AjaxResult updateUser(@RequestBody UserDTO user) {
        if (systemUserService.findById(user.getUserId()).isEmpty()) {
            return error("User not found");
        }
        user.setUpdateBy("admin");
        systemUserService.updateUser(user);
        return success("User updated successfully", systemUserService.findById(user.getUserId()).orElse(null));
    }

    @DeleteMapping("/system/user/{userId}")
    public AjaxResult removeUser(@PathVariable Long userId) {
        if (!systemUserService.deleteUser(userId)) {
            return error("User not found");
        }
        return success("User deleted successfully");
    }

    // ========== System: Role ==========

    @GetMapping("/system/role/list")
    public AjaxResult listRoles() {
        return success(systemRoleService.findAllActive());
    }

    @GetMapping("/system/role/{roleId}")
    public AjaxResult getRole(@PathVariable Long roleId) {
        return systemRoleService.findById(roleId)
                .map(this::success)
                .orElseGet(() -> error("Role not found"));
    }

    @PostMapping("/system/role")
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

    @PutMapping("/system/role")
    public AjaxResult updateRole(@RequestBody RoleDTO role) {
        if (systemRoleService.findById(role.getRoleId()).isEmpty()) {
            return error("Role not found");
        }
        role.setUpdateBy("admin");
        systemRoleService.updateRole(role);
        return success("Role updated successfully");
    }

    @DeleteMapping("/system/role/{roleId}")
    public AjaxResult removeRole(@PathVariable Long roleId) {
        if (!systemRoleService.deleteRole(roleId)) {
            return error("Role not found");
        }
        return success("Role deleted successfully");
    }

    // ========== System: Menu ==========

    @GetMapping("/system/menu/tree")
    public AjaxResult getMenuTree() {
        List<MenuDTO> menus = systemMenuService.findAllMenus();
        return success(buildMenuTree(menus, 0L));
    }

    @GetMapping("/system/menu/list")
    public AjaxResult listMenus() {
        return success(systemMenuService.findAllActive());
    }

    @GetMapping("/system/menu/{menuId}")
    public AjaxResult getMenu(@PathVariable Long menuId) {
        return systemMenuService.findById(menuId)
                .map(this::success)
                .orElseGet(() -> error("Menu not found"));
    }

    @PostMapping("/system/menu")
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

    @PutMapping("/system/menu")
    public AjaxResult updateMenu(@RequestBody MenuDTO menu) {
        if (systemMenuService.findById(menu.getMenuId()).isEmpty()) {
            return error("Menu not found");
        }
        menu.setUpdateBy("admin");
        systemMenuService.updateMenu(menu);
        return success("Menu updated successfully");
    }

    @DeleteMapping("/system/menu/{menuId}")
    public AjaxResult removeMenu(@PathVariable Long menuId) {
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

    @GetMapping("/system/dict/type/list")
    public AjaxResult listDictTypes() {
        return success(systemDictService.findAllActiveTypes());
    }

    @GetMapping("/system/dict/data/list")
    public AjaxResult listDictData(@RequestParam String dictType) {
        return success(systemDictService.findDataByTypeOrderBySort(dictType));
    }

    @GetMapping("/system/dict/data/type/{dictType}")
    public AjaxResult getDictDataByType(@PathVariable String dictType) {
        return success(systemDictService.findDataByTypeOrderBySort(dictType));
    }

    @GetMapping("/system/dict/type/get-by-code/{dictType}")
    public AjaxResult getDictTypeByCode(@PathVariable String dictType) {
        return systemDictService.findTypeByDictType(dictType)
                .map(this::success)
                .orElse(success(null));
    }

    @GetMapping("/system/dict/type/{dictId}")
    public AjaxResult getDictType(@PathVariable Long dictId) {
        return systemDictService.findTypeById(dictId)
                .map(this::success)
                .orElseGet(() -> error("Dictionary type not found"));
    }

    @PostMapping("/system/dict/type")
    public AjaxResult addDictType(@RequestBody DictTypeDTO dict) {
        dict.setStatus(dict.getStatus() != null ? dict.getStatus() : "0");
        dict.setCreateBy("admin");
        dict.setCreateTime(java.time.LocalDateTime.now());
        systemDictService.createType(dict);
        return success("Dictionary type added successfully");
    }

    @PutMapping("/system/dict/type")
    public AjaxResult updateDictType(@RequestBody DictTypeDTO dict) {
        if (systemDictService.findTypeById(dict.getDictId()).isEmpty()) {
            return error("Dictionary type not found");
        }
        dict.setUpdateBy("admin");
        dict.setUpdateTime(java.time.LocalDateTime.now());
        systemDictService.updateType(dict);
        return success("Dictionary type updated successfully");
    }

    @DeleteMapping("/system/dict/type/{dictId}")
    public AjaxResult removeDictType(@PathVariable Long dictId) {
        if (systemDictService.findTypeById(dictId).isEmpty()) {
            return error("Dictionary type not found");
        }
        systemDictService.deleteTypeById(dictId);
        return success("Dictionary type deleted successfully");
    }

    @PostMapping("/system/dict/data")
    public AjaxResult addDictData(@RequestBody DictDataDTO dictData) {
        dictData.setStatus(dictData.getStatus() != null ? dictData.getStatus() : "0");
        dictData.setCreateBy("admin");
        dictData.setCreateTime(java.time.LocalDateTime.now());
        systemDictService.createData(dictData);
        return success("Dictionary data added successfully");
    }

    @PutMapping("/system/dict/data")
    public AjaxResult updateDictData(@RequestBody DictDataDTO dictData) {
        if (dictData.getDictCode() == null || systemDictService.findDataById(dictData.getDictCode()).isEmpty()) {
            return error("Dictionary data not found");
        }
        dictData.setUpdateBy("admin");
        dictData.setUpdateTime(java.time.LocalDateTime.now());
        systemDictService.updateData(dictData);
        return success("Dictionary data updated successfully");
    }

    @DeleteMapping("/system/dict/data/{dictCode}")
    public AjaxResult removeDictData(@PathVariable Long dictCode) {
        if (systemDictService.findDataById(dictCode).isEmpty()) {
            return error("Dictionary data not found");
        }
        systemDictService.deleteDataById(dictCode);
        return success("Dictionary data deleted successfully");
    }

    // ========== System: Config ==========

    @GetMapping("/system/config/list")
    public AjaxResult listConfigs() {
        return success(systemConfigService.findAll());
    }

    @PostMapping("/system/config/batch")
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

    @PostMapping("/system/config/test-email")
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

    @GetMapping("/system/config/key/{configKey}")
    public AjaxResult getConfigByKey(@PathVariable String configKey) {
        return systemConfigService.findByConfigKey(configKey)
                .map(this::success)
                .orElseGet(() -> error("Config not found"));
    }

    @PutMapping("/system/config")
    public AjaxResult updateConfig(@RequestBody ConfigDTO config) {
        var existing = systemConfigService.findByConfigKey(config.getConfigKey()).orElse(null);
        if (existing == null) return error("Config not found");
        existing.setConfigValue(config.getConfigValue());
        existing.setUpdateBy("admin");
        systemConfigService.updateConfig(existing);
        return success("Configuration updated");
    }

    @DeleteMapping("/system/config/{configId}")
    public AjaxResult removeConfig(@PathVariable Long configId) {
        if (systemConfigService.findById(configId).isEmpty()) {
            return error("Config not found");
        }
        systemConfigService.deleteConfigByIds(new Long[]{configId});
        return success("Config deleted");
    }

    // ========== System: OperLog ==========

    @GetMapping("/system/operlog/list")
    public AjaxResult listOperLogs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String operName,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Integer status) {
        return success(systemOperLogService.findByCondition(title, operName, businessType, status));
    }

    @DeleteMapping("/system/operlog")
    public AjaxResult removeOperLogs(@RequestBody Long[] operIds) {
        systemOperLogService.deleteByIds(operIds);
        return success();
    }

    @DeleteMapping("/system/operlog/clean")
    public AjaxResult cleanOperLogs() {
        systemOperLogService.cleanLogs();
        return success();
    }

    // ========== System: Logininfor ==========

    @GetMapping("/system/logininfor/list")
    public AjaxResult listLoginInfos(
            @RequestParam(required = false) String loginName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ipaddr) {
        return success(systemLogininforService.findByCondition(loginName, status, ipaddr));
    }

    @DeleteMapping("/system/logininfor")
    public AjaxResult removeLoginInfos(@RequestBody Long[] infoIds) {
        systemLogininforService.deleteByIds(infoIds);
        return success();
    }

    @DeleteMapping("/system/logininfor/clean")
    public AjaxResult cleanLoginInfos() {
        systemLogininforService.cleanLogs();
        return success();
    }

    // ========== System: Notice ==========

    @GetMapping("/system/notice/list")
    public AjaxResult listNotices() {
        return success(systemNoticeService.findActiveNotices());
    }

    @GetMapping("/system/notice/{noticeId}")
    public AjaxResult getNotice(@PathVariable Integer noticeId) {
        return systemNoticeService.findById(noticeId.longValue())
                .map(this::success)
                .orElseGet(() -> error("Notice not found"));
    }

    @PostMapping("/system/notice")
    public AjaxResult addNotice(@RequestBody NoticeDTO notice) {
        notice.setStatus(notice.getStatus() != null ? notice.getStatus() : "0");
        notice.setCreateBy("admin");
        notice.setCreateTime(java.time.LocalDateTime.now());
        systemNoticeService.createNotice(notice);
        return success("Notice added successfully");
    }

    @PutMapping("/system/notice")
    public AjaxResult updateNotice(@RequestBody NoticeDTO notice) {
        if (systemNoticeService.findById(notice.getNoticeId()).isEmpty()) {
            return error("Notice not found");
        }
        notice.setUpdateBy("admin");
        notice.setUpdateTime(java.time.LocalDateTime.now());
        systemNoticeService.updateNotice(notice);
        return success("Notice updated successfully");
    }

    @DeleteMapping("/system/notice/{noticeId}")
    public AjaxResult removeNotice(@PathVariable Integer noticeId) {
        if (systemNoticeService.findById(noticeId.longValue()).isEmpty()) {
            return error("Notice not found");
        }
        systemNoticeService.deleteNoticeByIds(new Long[]{noticeId.longValue()});
        return success("Notice deleted successfully");
    }

    // ========== System: Notifications ==========

    @GetMapping("/system/notifications/list")
    public AjaxResult listNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = 1L;
        var page = systemNotificationService.getNotifications(userId, pageNum, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("unreadCount", systemNotificationService.getUnreadCount(userId));
        return success(result);
    }

    @GetMapping("/system/notifications/unread")
    public AjaxResult unreadNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = 1L;
        var page = systemNotificationService.getUnreadNotifications(userId, pageNum, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        return success(result);
    }

    @GetMapping("/system/notifications/unread-count")
    public AjaxResult unreadCount() {
        Long userId = 1L;
        return success(Map.of("count", systemNotificationService.getUnreadCount(userId)));
    }

    @PutMapping("/system/notifications/{notificationId}/read")
    public AjaxResult markAsRead(@PathVariable Long notificationId) {
        systemNotificationService.markAsRead(notificationId);
        return success();
    }

    @PutMapping("/system/notifications/read-all")
    public AjaxResult markAllAsRead() {
        systemNotificationService.markAllAsRead(1L);
        return success();
    }

    @GetMapping("/system/notifications/statistics")
    public AjaxResult notificationStatistics() {
        return success(systemNotificationService.getStatistics(1L));
    }

    @PostMapping("/system/notifications/test")
    public AjaxResult sendTestNotification(@RequestBody Map<String, String> params) {
        String title = params.get("title");
        String content = params.get("content");
        String type = params.getOrDefault("type", "INFO");
        systemNotificationService.sendInAppNotification(1L, title, content, type);
        return success("Notification sent");
    }

    // ========== System: Datasource ==========

    @GetMapping("/system/datasource/list")
    public AjaxResult listDatasources() {
        return success(systemDatasourceService.findAll());
    }

    @GetMapping("/system/datasource/{datasourceId}")
    public AjaxResult getDatasource(@PathVariable Long datasourceId) {
        return systemDatasourceService.findById(datasourceId)
                .map(this::success)
                .orElseGet(() -> error("Datasource not found"));
    }

    @PostMapping("/system/datasource")
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

    @PutMapping("/system/datasource")
    public AjaxResult updateDatasource(@RequestBody DatasourceDTO datasource) {
        if (systemDatasourceService.findById(datasource.getDatasourceId()).isEmpty()) {
            return error("Datasource not found");
        }
        systemDatasourceService.save(datasource);
        return success("Datasource updated successfully");
    }

    @DeleteMapping("/system/datasource/{datasourceId}")
    public AjaxResult removeDatasource(@PathVariable Long datasourceId) {
        systemDatasourceService.deleteById(datasourceId);
        return success("Datasource deleted successfully");
    }

    @PostMapping("/system/datasource/test")
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

    @GetMapping("/system/datasource/driver/{dbType}")
    public AjaxResult getDriverClass(@PathVariable String dbType) {
        Map<String, String> result = new HashMap<>();
        result.put("driverClass", systemDatasourceService.getDriverClass(dbType));
        result.put("urlPattern", systemDatasourceService.getDefaultUrlPattern(dbType));
        return success(result);
    }

    // ========== System: Email Config ==========

    @GetMapping("/system/email-config")
    public AjaxResult getEmailConfig() {
        return success(systemEmailConfigService.getConfig());
    }

    @PostMapping("/system/email-config")
    public AjaxResult saveEmailConfig(@RequestBody Map<String, Object> config) {
        return success(systemEmailConfigService.saveConfig(config));
    }

    @PostMapping("/system/email-config/test")
    public AjaxResult testEmailConfig(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        if (to == null || to.isEmpty()) return error("Recipient email is required");
        return success(systemEmailConfigService.sendTestEmail(to));
    }

    // ========== System: Cache ==========

    @GetMapping("/system/cache/list")
    public AjaxResult listCaches() {
        return success(systemCacheService.listCaches());
    }

    @PostMapping("/system/cache/clear/{cacheName}")
    public AjaxResult clearCache(@PathVariable String cacheName) {
        return success(systemCacheService.clearCache(cacheName));
    }

    @PostMapping("/system/cache/clear-all")
    public AjaxResult clearAllCaches() {
        return success(systemCacheService.clearAllCaches());
    }

    @GetMapping("/system/cache/stats/{cacheName}")
    public AjaxResult getCacheStats(@PathVariable String cacheName) {
        return success(systemCacheService.getCacheStats(cacheName));
    }

    // ========== System: Chat ==========

    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> toolResults = (List<Map<String, Object>>) request.getOrDefault("toolResults", null);
        return success(systemChatService.chat(message, toolResults, getCurrentUsername()));
    }

    @GetMapping("/chat/status")
    public AjaxResult chatStatus() {
        return success(systemChatService.chatStatus());
    }

    @PostMapping("/chat/knowledge/refresh")
    public AjaxResult refreshKnowledge() {
        return success(systemChatService.refreshKnowledge());
    }

    @GetMapping("/chat/knowledge/stats")
    public AjaxResult knowledgeStats() {
        return success(systemChatService.knowledgeStats());
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        return systemChatService.chatStream(message, getCurrentUsername());
    }

    @GetMapping("/chat/history")
    public AjaxResult getConversationHistory() {
        return success(systemChatService.getConversationHistory(getCurrentUsername()));
    }

    @PostMapping("/chat/clear-memory")
    public AjaxResult clearMemory() {
        return success(systemChatService.clearMemory(getCurrentUsername()));
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

    // ========== System: Report ==========

    @GetMapping("/system/report/list")
    public AjaxResult listReports() {
        return success(systemReportEntityService.findAllReports());
    }

    @GetMapping("/system/report/{reportId}")
    public AjaxResult getReport(@PathVariable Long reportId) {
        return Optional.ofNullable(systemReportEntityService.findById(reportId))
                .map(this::success)
                .orElseGet(() -> error("Report not found"));
    }

    @PostMapping("/system/report")
    public AjaxResult addReport(@RequestBody Object report) {
        return success(systemReportEntityService.createReport(report));
    }

    @PutMapping("/system/report")
    public AjaxResult updateReport(@RequestBody Object report) {
        return success(systemReportEntityService.updateReport(report));
    }

    @DeleteMapping("/system/report/{reportId}")
    public AjaxResult removeReport(@PathVariable Long reportId) {
        return success(systemReportEntityService.deleteReport(reportId));
    }

    @PostMapping("/system/report/execute/{reportId}")
    public AjaxResult executeReport(@PathVariable Long reportId, @RequestBody(required = false) Map<String, Object> params) {
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

    @GetMapping("/system/report/templates")
    public AjaxResult getReportTemplates() {
        return success(systemReportEntityService.getTemplates());
    }

    @GetMapping("/system/report/template/{templateId}")
    public AjaxResult getReportTemplate(@PathVariable Long templateId) {
        return Optional.ofNullable(systemReportEntityService.getTemplate(templateId))
                .map(this::success)
                .orElseGet(() -> error("Template not found"));
    }

    @PostMapping("/system/report/from-template")
    public AjaxResult createReportFromTemplate(@RequestBody Map<String, Object> request) {
        return success(systemReportEntityService.createReportFromTemplate(request));
    }

    @PostMapping("/system/report/schedule/{templateId}")
    public AjaxResult scheduleReport(@PathVariable Long templateId, @RequestBody Map<String, Object> config) {
        return success(systemReportEntityService.scheduleReport(templateId, config));
    }

    @DeleteMapping("/system/report/unschedule/{reportId}")
    public AjaxResult unscheduleReport(@PathVariable Long reportId) {
        return success(systemReportEntityService.unscheduleReport(reportId));
    }

    // ========== System: Report Designer ==========

    @GetMapping("/system/report-designer/templates")
    public AjaxResult listReportDesignerTemplates(@RequestParam(required = false, defaultValue = "false") Boolean allVersions) {
        // ReportDesignerService is already an API, inject it separately
        return success(new ArrayList<>()); // TODO: inject ReportDesignerService
    }

    @GetMapping("/system/report-designer/templates/{templateId}")
    public AjaxResult getReportDesignerTemplate(@PathVariable Long templateId) {
        return error("Template not found"); // TODO: inject ReportDesignerService
    }

    @GetMapping("/system/report-designer/templates/key/{templateKey}")
    public AjaxResult getReportDesignerTemplateByKey(@PathVariable String templateKey) {
        return error("Template not found"); // TODO: inject ReportDesignerService
    }

    @PostMapping("/system/report-designer/templates")
    public AjaxResult addReportDesignerTemplate(@RequestBody Object template) {
        return success("Template added"); // TODO: inject ReportDesignerService
    }

    @PutMapping("/system/report-designer/templates")
    public AjaxResult updateReportDesignerTemplate(@RequestBody Object template) {
        return success("Template updated"); // TODO: inject ReportDesignerService
    }

    @DeleteMapping("/system/report-designer/templates/{templateId}")
    public AjaxResult deleteReportDesignerTemplate(@PathVariable Long templateId) {
        return success("Template deleted"); // TODO: inject ReportDesignerService
    }

    @GetMapping("/system/report-designer/templates/{templateKey}/versions")
    public AjaxResult getReportDesignerTemplateVersions(@PathVariable String templateKey) {
        return success(new ArrayList<>()); // TODO: inject ReportDesignerService
    }

    @GetMapping("/system/report-designer/templates/active-versions")
    public AjaxResult getReportDesignerActiveVersions() {
        return success(new ArrayList<>()); // TODO: inject ReportDesignerService
    }

    @GetMapping("/system/report-designer/active-templates")
    public AjaxResult getReportDesignerActiveTemplates() {
        return success(new ArrayList<>()); // TODO: inject ReportDesignerService
    }

    @PutMapping("/system/report-designer/templates/{templateId}/archive")
    public AjaxResult archiveReportDesignerTemplate(@PathVariable Long templateId) {
        return success("Template archived"); // TODO: inject ReportDesignerService
    }

    @PutMapping("/system/report-designer/templates/{templateId}/activate")
    public AjaxResult activateReportDesignerTemplate(@PathVariable Long templateId) {
        return success("Template activated"); // TODO: inject ReportDesignerService
    }

    @GetMapping("/system/report-designer/datasource/{datasourceKey}/tables")
    public AjaxResult getDatasourceTables(@PathVariable String datasourceKey) {
        return success(new ArrayList<>()); // TODO: inject ReportDesignerService
    }

    @PostMapping("/system/report-designer/execute/{templateId}")
    public AjaxResult executeReportDesignerTemplate(@PathVariable Long templateId, @RequestBody(required = false) Map<String, Object> params) {
        return error("Failed to execute report"); // TODO: inject ReportDesignerService
    }

    @PostMapping("/system/report-designer/preview")
    public AjaxResult previewReport(@RequestBody Object template, @RequestParam(required = false) String params) {
        return error("Preview failed"); // TODO: inject ReportDesignerService
    }

    // ========== Quartz: Job ==========

    @GetMapping("/system/job/list")
    public AjaxResult listJobs() {
        return success(quartzJobService.findAll());
    }

    @GetMapping("/system/job/{jobId}")
    public AjaxResult getJob(@PathVariable Long jobId) {
        return quartzJobService.findById(jobId)
                .map(this::success)
                .orElseGet(() -> error("Job not found"));
    }

    @PostMapping("/system/job")
    public AjaxResult addJob(@RequestBody JobDTO job) {
        quartzJobService.createJob(job);
        return success("Job added successfully");
    }

    @PutMapping("/system/job")
    public AjaxResult updateJob(@RequestBody JobDTO job) {
        quartzJobService.updateJob(job);
        return success("Job updated successfully");
    }

    @DeleteMapping("/system/job/{jobId}")
    public AjaxResult removeJob(@PathVariable Long jobId) {
        quartzJobService.deleteJob(jobId);
        return success("Job deleted successfully");
    }

    @DeleteMapping("/system/job/batch")
    public AjaxResult batchRemoveJobs(@RequestBody Long[] ids) {
        for (Long id : ids) {
            quartzJobService.deleteJob(id);
        }
        return success("Deleted " + ids.length + " job(s)");
    }

    @PutMapping("/system/job/changeStatus")
    public AjaxResult changeJobStatus(@RequestBody JobDTO job) {
        quartzJobService.updateJob(job);
        return success("Job status updated successfully");
    }

    @PostMapping("/system/job/run")
    public AjaxResult runJob(@RequestBody Map<String, Object> request) {
        Long jobId = request.get("jobId") != null ? ((Number) request.get("jobId")).longValue() : null;
        if (jobId == null) return error("Job ID is required");
        quartzJobService.runJob(jobId);
        return success();
    }

    @PutMapping("/system/job/pause")
    public AjaxResult pauseJob(@RequestBody JobDTO job) {
        quartzJobService.pauseJob(job.getJobId());
        return success("Job paused successfully");
    }

    @PutMapping("/system/job/resume")
    public AjaxResult resumeJob(@RequestBody JobDTO job) {
        quartzJobService.resumeJob(job.getJobId());
        return success("Job resumed successfully");
    }

    @PutMapping("/system/job/batch/pause")
    public AjaxResult batchPauseJobs(@RequestBody Long[] ids) {
        for (Long id : ids) {
            quartzJobService.pauseJob(id);
        }
        return success("Paused " + ids.length + " job(s)");
    }

    @PutMapping("/system/job/batch/resume")
    public AjaxResult batchResumeJobs(@RequestBody Long[] ids) {
        for (Long id : ids) {
            quartzJobService.resumeJob(id);
        }
        return success("Resumed " + ids.length + " job(s)");
    }

    @PostMapping("/system/job/batch/run")
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

    @GetMapping("/system/job/export")
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

    @PostMapping("/system/job/import")
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

    @GetMapping("/system/job/groups")
    public AjaxResult getJobGroups() {
        List<String> groups = quartzJobService.findAll().stream()
                .map(JobDTO::getJobGroup)
                .distinct()
                .toList();
        return success(groups);
    }

    // ========== Quartz: Job Log ==========

    @GetMapping("/system/job-log/list")
    public AjaxResult listJobLogs(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobGroup,
            @RequestParam(required = false) String status) {
        List<JobLogDTO> logs;
        if (jobName != null) {
            logs = quartzJobLogService.findByJobName(jobName);
        } else if (status != null) {
            logs = quartzJobLogService.findByStatus(status);
        } else {
            logs = quartzJobLogService.findAll();
        }
        return success(logs);
    }

    @GetMapping("/system/job-log/job/{jobId}")
    public AjaxResult getJobLogByJobId(@PathVariable Long jobId) {
        var jobOpt = quartzJobLogService.findById(jobId);
        return jobOpt.map(j -> success(List.of(j))).orElseGet(() -> error("Job log not found"));
    }

    @GetMapping("/system/job-log/{logId}")
    public AjaxResult getJobLogById(@PathVariable Long logId) {
        return quartzJobLogService.findById(logId)
                .map(this::success)
                .orElseGet(() -> error("Job log not found"));
    }

    @GetMapping("/system/job-log/failed/recent")
    public AjaxResult getRecentFailedLogs(@RequestParam(defaultValue = "10") int limit) {
        var logs = quartzJobLogService.findAll().stream()
                .filter(l -> "1".equals(l.getStatus()))
                .limit(limit)
                .toList();
        return success(logs);
    }

    @DeleteMapping("/system/job-log/{logId}")
    public AjaxResult removeJobLog(@PathVariable Long logId) {
        quartzJobLogService.deleteByIds(new Long[]{logId});
        return success("Job log deleted successfully");
    }

    @DeleteMapping("/system/job-log/batch")
    public AjaxResult batchRemoveJobLogs(@RequestBody Long[] ids) {
        quartzJobLogService.deleteByIds(ids);
        return success("Deleted " + ids.length + " job log(s)");
    }

    @DeleteMapping("/system/job-log/clean")
    public AjaxResult cleanJobLogs() {
        quartzJobLogService.cleanLogs();
        return success("All job logs cleared");
    }

    // ========== Quartz: Job Template ==========

    @GetMapping("/system/job-template/list")
    public AjaxResult listJobTemplates() {
        return success(quartzJobTemplateService.getTemplates());
    }

    @GetMapping("/system/job-template/{name}")
    public AjaxResult getJobTemplate(@PathVariable String name) {
        var template = quartzJobTemplateService.getTemplateByName(name);
        return template != null ? success(template) : error("Template not found");
    }

    @PostMapping("/system/job-template/create/{templateName}")
    public AjaxResult createJobFromTemplate(@PathVariable String templateName, @RequestParam(required = false) String jobName) {
        JobDTO job = quartzJobTemplateService.createJobFromTemplate(templateName, jobName);
        quartzJobService.createJob(job);
        return success("Job created from template: " + templateName);
    }

    // ========== Quartz: Email Template ==========

    @GetMapping("/system/email-template/list")
    public AjaxResult listEmailTemplates() {
        return success(quartzEmailJobTemplateService.getAllTemplates());
    }

    @GetMapping("/system/email-template/active")
    public AjaxResult getActiveEmailTemplates() {
        return success(quartzEmailJobTemplateService.getActiveTemplates());
    }

    @GetMapping("/system/email-template/{templateId}")
    public AjaxResult getEmailTemplate(@PathVariable Long templateId) {
        return quartzEmailJobTemplateService.getTemplateById(templateId)
                .map(this::success)
                .orElseGet(() -> error("Template not found"));
    }

    @GetMapping("/system/email-template/type/{templateType}")
    public AjaxResult getEmailTemplateByType(@PathVariable String templateType) {
        return quartzEmailJobTemplateService.getTemplateByType(templateType)
                .map(this::success)
                .orElseGet(() -> error("Template not found for type: " + templateType));
    }

    @PostMapping("/system/email-template")
    public AjaxResult addEmailTemplate(@RequestBody EmailTemplateDTO template) {
        quartzEmailJobTemplateService.saveTemplate(template);
        return success("Template created successfully");
    }

    @PutMapping("/system/email-template")
    public AjaxResult updateEmailTemplate(@RequestBody EmailTemplateDTO template) {
        quartzEmailJobTemplateService.saveTemplate(template);
        return success("Template updated successfully");
    }

    @DeleteMapping("/system/email-template/{templateId}")
    public AjaxResult removeEmailTemplate(@PathVariable Long templateId) {
        quartzEmailJobTemplateService.deleteTemplate(templateId);
        return success("Template deleted successfully");
    }

    @PutMapping("/system/email-template/{templateId}/set-default")
    public AjaxResult setEmailTemplateAsDefault(@PathVariable Long templateId, @RequestParam String templateType) {
        quartzEmailJobTemplateService.setTemplateAsDefault(templateId, templateType);
        return success("Template set as default");
    }

    @PutMapping("/system/email-template/{templateId}/toggle-active")
    public AjaxResult toggleEmailTemplateActive(@PathVariable Long templateId) {
        EmailTemplateDTO template = quartzEmailJobTemplateService.getTemplateById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        template.setIsActive(!template.getIsActive());
        quartzEmailJobTemplateService.saveTemplate(template);
        return success("Template status updated");
    }

    @PostMapping("/system/email-template/preview")
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

    @GetMapping("/system/job-group/list")
    public AjaxResult getJobGroupsSummary() {
        return success(quartzJobGroupService.getJobGroupSummary());
    }

    @GetMapping("/system/job-group/{jobGroup}/jobs")
    public AjaxResult getJobsInGroup(@PathVariable String jobGroup) {
        return success(quartzJobGroupService.getJobsInGroup(jobGroup));
    }

    @PostMapping("/system/job-group/{jobGroup}/execute")
    public AjaxResult executeJobGroup(@PathVariable String jobGroup) {
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

    @GetMapping("/system/job-dashboard/metrics")
    public AjaxResult getJobMetrics() {
        return success(quartzJobMetricsService.getDashboardMetrics());
    }

    @GetMapping("/system/job-dashboard/trend")
    public AjaxResult getJobTrend(@RequestParam(defaultValue = "30") int days) {
        return success(quartzJobMetricsService.getExecutionTrend(days));
    }

    @GetMapping("/system/job-dashboard/health")
    public AjaxResult getJobHealth() {
        return success(quartzJobMetricsService.getJobHealth());
    }

    // ========== Quartz: Script Job ==========

    @PostMapping("/system/scriptJob/run")
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

    @PostMapping("/public/job/webhook/{jobId}")
    public AjaxResult triggerJobWebhook(@PathVariable Long jobId, @RequestParam String token) {
        return success(quartzJobWebhookService.triggerJobByWebhook(jobId, token));
    }

    // ========== Generator ==========

    @GetMapping("/tool/gen/db/tables")
    public AjaxResult listTables() {
        try {
            return success(generatorService.findAllTables());
        } catch (Exception e) {
            return error("Failed to load tables: " + e.getMessage());
        }
    }

    @GetMapping("/tool/gen/preview")
    public void preview(@RequestParam Long tableId, HttpServletResponse response) throws IOException {
        Map<String, String> code = generatorService.previewCode(tableId);
        response.setContentType("text/plain;charset=UTF-8");
        if (code != null && !code.isEmpty()) {
            response.getWriter().write(code.values().iterator().next());
        }
    }

    @PostMapping("/tool/gen/batch")
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

    @GetMapping("/tool/gen/download")
    public void download(@RequestParam Long tableId, HttpServletResponse response) throws IOException {
        try {
            byte[] zipBytes = generatorService.downloadCode(tableId);
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=code.zip");
            response.getOutputStream().write(zipBytes);
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Error: " + e.getMessage());
        }
    }

    // ========== SPA Forwarding ==========

    // Note: SPA forwarding is handled by a separate internal controller
}
