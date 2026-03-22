package com.pd.framework.ai.service;

import com.pd.framework.ai.domain.AiKnowledge;
import com.pd.framework.ai.infrastructure.repository.AiKnowledgeRepository;
import com.pd.framework.ai.service.AiChatService.KnowledgeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Knowledge Base Service
 * Manages system documentation for AI RAG
 */
@Service
public class KnowledgeBaseService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseService.class);

    private final AiKnowledgeRepository knowledgeRepository;
    private final AiChatService aiChatService;

    @Autowired
    public KnowledgeBaseService(
            AiKnowledgeRepository knowledgeRepository,
            AiChatService aiChatService
    ) {
        this.knowledgeRepository = knowledgeRepository;
        this.aiChatService = aiChatService;
    }

    @Override
    public void run(String... args) {
        // Initialize knowledge base on startup
        if (knowledgeRepository.count() == 0) {
            log.info("Initializing AI knowledge base with system documentation...");
            initializeKnowledgeBase();
        }
    }

    @Transactional
    public void initializeKnowledgeBase() {
        List<AiKnowledge> documents = getSystemDocumentation();
        knowledgeRepository.saveAll(documents);
        log.info("Saved {} knowledge documents", documents.size());

        // Also load into embedding store for RAG
        List<KnowledgeDocument> ragDocuments = documents.stream()
                .map(k -> new KnowledgeDocument(k.getTitle(), k.getContent(), k.getCategory()))
                .toList();
        aiChatService.addKnowledgeBatch(ragDocuments);
    }

    /**
     * Get system documentation for knowledge base
     */
    private List<AiKnowledge> getSystemDocumentation() {
        List<AiKnowledge> docs = new ArrayList<>();

        // User Management
        docs.add(new AiKnowledge(
                "User Management Overview",
                """
                The Vantage Admin platform provides comprehensive user management capabilities.

                Key Features:
                - Create, update, and delete user accounts
                - Assign roles and permissions to users
                - Track login history and operation logs
                - Manage user status (active/inactive)
                - Support for custom user fields

                User Entity Fields:
                - loginName: Unique login identifier
                - userName: Display name
                - email: User email address
                - phonenumber: Contact number
                - sex: Gender (0=unknown, 1=male, 2=female)
                - status: Account status (0=active, 1=disabled)
                - remark: Additional notes

                API Endpoints:
                - POST /api/system/user - Create user
                - PUT /api/system/user - Update user
                - DELETE /api/system/user/{id} - Delete user
                - GET /api/system/user/list - List all users
                - GET /api/system/user/{id} - Get user details
                """,
                "user-management"
        ));

        docs.add(new AiKnowledge(
                "How to Create a User",
                """
                Creating a new user in Vantage Admin:

                Via API:
                POST /api/system/user
                Content-Type: application/json

                {
                  "loginName": "john.doe",
                  "userName": "John Doe",
                  "password": "securePassword123",
                  "email": "john@example.com",
                  "phonenumber": "1234567890",
                  "sex": "1",
                  "status": "0",
                  "remark": "New user"
                }

                Via UI:
                1. Navigate to System > User Management
                2. Click "New" or "Create User" button
                3. Fill in required fields (login name, user name, password)
                4. Optionally assign roles
                5. Click "Save"

                Password Requirements:
                - Minimum 6 characters
                - BCrypt hashed before storage
                - Use HashGen.java to generate hashes manually

                After creation, the user can log in with their credentials.
                """,
                "user-management"
        ));

        // Role Management
        docs.add(new AiKnowledge(
                "Role Management Overview",
                """
                Role-based access control (RBAC) in Vantage Admin:

                Key Concepts:
                - Roles group permissions together
                - Users are assigned to roles
                - Permissions control access to features
                - Menu visibility is role-based

                Role Entity Fields:
                - roleName: Display name (e.g., "Administrator")
                - roleKey: Unique identifier (e.g., "admin")
                - roleSort: Display order
                - status: Role status (0=active, 1=disabled)
                - dataScope: Data visibility scope

                Common Roles:
                - admin: Full system access
                - user: Basic user access
                - guest: Read-only access

                API Endpoints:
                - POST /api/system/role - Create role
                - PUT /api/system/role - Update role
                - DELETE /api/system/role/{id} - Delete role
                - GET /api/system/role/list - List all roles
                """,
                "role-management"
        ));

        // Menu Management
        docs.add(new AiKnowledge(
                "Menu Management",
                """
                Configure navigation menus in Vantage Admin:

                Menu Types:
                - M: Menu (parent/top-level menu)
                - C: Sub Menu (child menu with page)
                - F: Permission (button/action permission)

                Menu Fields:
                - menuName: Display name
                - parentId: Parent menu ID (0 for root)
                - orderNum: Display order
                - url: Route path
                - perms: Permission identifier
                - icon: Menu icon
                - visible: Visibility (0=visible, 1=hidden)

                Menu Structure Example:
                - System (M)
                  - User Management (C) /system/user
                  - Role Management (C) /system/role
                  - Menu Management (C) /system/menu
                  - Config Management (C) /system/config

                Permissions follow pattern: module:action
                Example: system:user:list, system:user:add
                """,
                "menu-management"
        ));

        // Operation Logging
        docs.add(new AiKnowledge(
                "Operation Logging",
                """
                Automatic operation logging via AOP:

                Features:
                - Logs all REST API calls (POST, PUT, DELETE)
                - Records user, IP, browser, OS
                - Tracks execution time
                - Captures request/response data

                @Log Annotation:
                @Log(title = "User Management", businessType = BusinessType.INSERT)

                Business Types:
                - INSERT: Create operations
                - UPDATE: Update operations
                - DELETE: Delete operations
                - EXPORT: Data export
                - IMPORT: Data import

                Logged Information:
                - operName: Operator username
                - operUrl: API endpoint
                - operIp: Client IP address
                - operParam: Request parameters
                - jsonResult: Response data
                - status: 0=success, 1=failure
                - errorMsg: Error message (if failed)
                - costTime: Execution time in ms

                View logs at: System > Operation Logs
                """,
                "operation-log"
        ));

        // Job Scheduling
        docs.add(new AiKnowledge(
                "Job Scheduling with Quartz",
                """
                Quartz-based job scheduling in Vantage Admin:

                Job Entity Fields:
                - jobName: Job display name
                - jobGroup: Job group (DEFAULT, SYSTEM, etc.)
                - invokeTarget: Method to invoke
                - cronExpression: Cron schedule
                - misfirePolicy: Missed execution handling
                - concurrent: Allow concurrent execution (0=yes, 1=no)
                - status: Job status (0=active, 1=paused)

                Cron Expression Format:
                sec min hour day month weekday
                Example: 0 0 12 * * ? (every day at 12:00)

                Common Schedules:
                - Every 5 minutes: 0 0/5 * * * ?
                - Every hour: 0 0 * * * ?
                - Daily at midnight: 0 0 0 * * ?
                - Weekly on Monday: 0 0 0 ? * MON

                Misfire Policies:
                - 1: Execute immediately
                - 2: Execute once at next scheduled time
                - 3: Do nothing (skip missed executions)

                API Endpoints:
                - POST /quartz/jobs - Create job
                - PUT /quartz/jobs - Update job
                - DELETE /quartz/jobs/{id} - Delete job
                - PUT /quartz/jobs/run - Run job manually
                - PUT /quartz/jobs/pause - Pause job
                - PUT /quartz/jobs/resume - Resume job
                """,
                "job-scheduling"
        ));

        // System Configuration
        docs.add(new AiKnowledge(
                "System Configuration",
                """
                Manage system-wide configuration settings:

                Config Entity Fields:
                - configName: Display name
                - configKey: Unique identifier
                - configValue: Configuration value
                - configType: System-defined (Y) or user-defined (N)

                Common Configurations:
                - sys.account.initPassword: Initial password for new users
                - sys.account.unlockTime: Account unlock time after lockout
                - sys.user.maxRetryCount: Max login retry attempts
                - sys.file.upload.path: File upload directory

                API Endpoints:
                - POST /api/system/config - Create config
                - PUT /api/system/config - Update config
                - DELETE /api/system/config/{id} - Delete config
                - GET /api/system/config/list - List all configs
                - GET /api/system/config/{key} - Get config by key

                Config values are cached for performance.
                Use cache refresh after updates.
                """,
                "system-config"
        ));

        // Dictionary Management
        docs.add(new AiKnowledge(
                "Dictionary Management",
                """
                Data dictionary for standardized values:

                Dict Type Fields:
                - dictName: Display name
                - dictType: Unique type identifier

                Dict Data Fields:
                - dictLabel: Display label
                - dictValue: Stored value
                - dictSort: Sort order
                - cssClass: CSS class for styling
                - listClass: List component style
                - isDefault: Default option (Y/N)

                Common Dictionary Types:
                - sys_user_sex: User gender
                - sys_normal_disable: Status (normal/disabled)
                - sys_yes_no: Yes/No options
                - sys_job_group: Job groups

                Example - Gender Dictionary:
                - Type: sys_user_sex
                - Data:
                  - Label: Male, Value: 1
                  - Label: Female, Value: 2
                  - Label: Unknown, Value: 0

                API Endpoints:
                - GET /api/system/dict/type/list - List dict types
                - GET /api/system/dict/data/{type} - Get dict data by type
                """,
                "dict-management"
        ));

        // Code Generation
        docs.add(new AiKnowledge(
                "Code Generation",
                """
                Generate CRUD code from database tables:

                Features:
                - Generate entity classes
                - Generate repository interfaces
                - Generate service layer
                - Generate controllers
                - Generate React frontend components

                Gen Table Fields:
                - tableName: Database table name
                - tableComment: Table description
                - className: Java class name
                - tplCategory: Template (crud, tree, sub)
                - packageName: Java package
                - moduleName: Module name
                - businessName: Business entity name
                - functionAuthor: Author name

                Gen Table Column Fields:
                - columnName: Database column
                - columnComment: Column description
                - javaType: Java type
                - javaField: Java field name
                - isPk: Primary key (Y/N)
                - isIncrement: Auto-increment (Y/N)
                - isRequired: Required field (Y/N)
                - isInsert: Include in insert (Y/N)
                - isEdit: Editable (Y/N)
                - isList: Show in list (Y/N)
                - isQuery: Queryable (Y/N)
                - queryType: Query type (EQ, LIKE, GT, LT)
                - htmlType: HTML input type

                Generation Steps:
                1. Import table from database
                2. Configure generation settings
                3. Preview generated code
                4. Download or generate to project

                API Endpoints:
                - GET /generator/list - List generatable tables
                - POST /generator/import/{tableName} - Import table
                - POST /generator/code/{tableName} - Generate code
                """,
                "code-generation"
        ));

        // Login Monitoring
        docs.add(new AiKnowledge(
                "Login Monitoring",
                """
                Track and monitor user login activity:

                Login Info Fields:
                - loginName: Username
                - status: Login result (0=success, 1=failure)
                - ipaddr: IP address
                - loginLocation: Geographic location
                - browser: Browser name
                - os: Operating system
                - msg: Message (error reason)
                - loginTime: Timestamp

                Security Features:
                - Failed login tracking
                - Account lockout after max retries
                - IP-based access control
                - Login history audit

                Common Failure Reasons:
                - Invalid password
                - Account disabled
                - Account locked
                - IP not allowed

                View login logs at: System > Login Logs

                API Endpoints:
                - GET /api/system/logininfor/list - List login records
                - DELETE /api/system/logininfor/clean - Clear old records
                - DELETE /api/system/logininfor/{ids} - Delete specific records
                """,
                "login-monitoring"
        ));

        // Security Best Practices
        docs.add(new AiKnowledge(
                "Security Best Practices",
                """
                Security recommendations for Vantage Admin:

                Password Policy:
                - Minimum 8 characters recommended
                - Mix of uppercase, lowercase, numbers, symbols
                - Change password every 90 days
                - Never reuse last 5 passwords

                Account Security:
                - Enable account lockout after 5 failed attempts
                - Unlock time: 10 minutes recommended
                - Monitor login logs for suspicious activity
                - Disable inactive accounts after 90 days

                Role-Based Access:
                - Follow principle of least privilege
                - Regular role access reviews
                - Separate admin and user accounts
                - Audit role assignments quarterly

                Data Protection:
                - Enable HTTPS in production
                - Encrypt sensitive data at rest
                - Regular database backups
                - Implement data retention policies

                Session Management:
                - Session timeout: 30 minutes recommended
                - Force logout on password change
                - Limit concurrent sessions per user
                - Clear session on logout
                """,
                "security"
        ));

        // Troubleshooting Guide
        docs.add(new AiKnowledge(
                "Troubleshooting Guide",
                """
                Common issues and solutions:

                Login Issues:
                - "Invalid credentials": Check username/password
                - "Account locked": Wait for unlock time or contact admin
                - "Account disabled": Contact administrator to enable
                - "IP not allowed": Check IP access control settings

                Performance Issues:
                - Slow page load: Clear browser cache, check network
                - Slow queries: Check database indexes, optimize queries
                - High memory usage: Increase JVM heap size

                Common Errors:
                - 401 Unauthorized: Session expired, login again
                - 403 Forbidden: Insufficient permissions
                - 404 Not Found: Check URL/route
                - 500 Internal Server Error: Check server logs

                Cache Issues:
                - Stale data: Refresh cache in System > Cache
                - UI not updating: Hard refresh (Ctrl+Shift+R)

                Logging:
                - Check app.log for application errors
                - Enable DEBUG mode for detailed logging
                - Monitor operation logs for failed requests
                """,
                "troubleshooting"
        ));

        // API Documentation
        docs.add(new AiKnowledge(
                "API Documentation",
                """
                REST API overview for Vantage Admin:

                Authentication:
                - Session-based authentication
                - Login via POST /api/login
                - Logout via POST /api/logout
                - Current user via GET /api/me

                Response Format:
                {
                  "code": 200,
                  "msg": "success",
                  "data": { ... }
                }

                Response Codes:
                - 200: Success
                - 400: Bad Request
                - 401: Unauthorized
                - 403: Forbidden
                - 404: Not Found
                - 500: Internal Server Error

                Pagination:
                - pageNum: Page number (1-based)
                - pageSize: Items per page (default: 10)
                - total: Total count returned in response

                Common Endpoints:
                - /api/system/user - User management
                - /api/system/role - Role management
                - /api/system/menu - Menu management
                - /api/system/config - Config management
                - /api/system/dict - Dictionary management
                - /quartz/jobs - Job scheduling
                - /generator - Code generation
                """,
                "api-documentation"
        ));

        return docs;
    }

    /**
     * Get knowledge by category
     */
    public List<AiKnowledge> getByCategory(String category) {
        return knowledgeRepository.findByCategoryAndStatusOrderByCreateTimeDesc(category, "1");
    }

    /**
     * Get all active knowledge
     */
    public List<AiKnowledge> getAllActive() {
        return knowledgeRepository.findByStatusOrderByCreateTimeDesc("1");
    }

    /**
     * Search knowledge
     */
    public List<AiKnowledge> search(String keyword) {
        return knowledgeRepository.searchByKeyword(keyword);
    }

    /**
     * Get knowledge statistics
     */
    public KnowledgeStats getStats() {
        long total = knowledgeRepository.countByCategoryAndStatus("", "1");
        long userManagement = knowledgeRepository.countByCategoryAndStatus("user-management", "1");
        long roleManagement = knowledgeRepository.countByCategoryAndStatus("role-management", "1");
        long jobScheduling = knowledgeRepository.countByCategoryAndStatus("job-scheduling", "1");
        
        return new KnowledgeStats(total, userManagement, roleManagement, jobScheduling);
    }

    public record KnowledgeStats(long total, long userManagement, long roleManagement, long jobScheduling) {}
}
