# Vantage Admin - Complete Documentation

## Table of Contents
1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Architecture](#architecture)
4. [Features](#features)
5. [API Reference](#api-reference)
6. [Database Schema](#database-schema)
7. [Spring Boot Actuator](#spring-boot-actuator)
8. [Spring Modulith Architecture](#spring-modulith-architecture)
9. [Gateway Pattern](#gateway-pattern)
10. [Testing Strategy](#testing-strategy)
11. [Troubleshooting](#troubleshooting)

---

## Overview

**Vantage Admin** is a comprehensive enterprise administration platform built with **Spring Boot 4.0.5**, **Spring Modulith 2.0.5**, and **React 19**. It provides user/role/menu management, Quartz job scheduling, report generation, code generation, and an AI-powered chat assistant.

### Architecture Evolution (v2.0)

Vantage has been refactored from a multi-module Maven project into a **single JAR modulith** with **gateway-only external routing**. Key changes:

- **Merged Maven modules** — `vantage-common`, `vantage-modules/*` consolidated into `vantage-admin`
- **Gateway pattern** — `GatewayManagement` is the sole `@RestController`; all module controllers removed
- **API-first communication** — modules expose only `api/` interfaces; no cross-module service injection
- **UI separation** — frontend is a separate project; backend serves no static assets

### Tech Stack
- **Backend:** Spring Boot 4.0.5, Spring Modulith 2.0.5, Spring Data JPA, Quartz Scheduler
- **Frontend:** React 19, Vite 5, Glassmorphism UI, Lucide Icons (separate project)
- **Database:** H2 (file-based), supports PostgreSQL/MySQL/Oracle
- **Security:** Spring Security with session-based authentication (BCrypt)
- **AI:** LangChain4j 0.35.0 with Ollama (Qwen2.5-Coder 0.5b)
- **Monitoring:** Spring Boot Actuator + Micrometer (Prometheus)

### Key Features
- User & Role Management with RBAC
- Dynamic Menu & Permission System
- Quartz Job Scheduling with retries, timeouts, dependencies, and webhook triggers
- Report Generation with SQL, parameters, and dynamic system variables
- Code Generation from database tables
- Email Templates with dynamic data tables
- AI Chat Assistant with RAG knowledge base
- Operation & Login Audit Logging
- Dictionary & Config Management
- Real-time Monitoring Dashboard (actuator-backed)
- Script Job Execution (SQL, JavaScript, Groovy, Python)

---

## Getting Started

### Prerequisites
- **Java 17** (required — JDK 17 compatible, no Java 21+ features used)
- **Node.js 20.11+** (for Vite)
- **Maven 3.8+**

### Build & Run

#### Backend Only
```bash
mvn clean package -DskipTests
cd vantage-admin
mvn spring-boot:run
```

#### UI Development (separate dev server)
```bash
cd vantage-ui
npm install
npm run dev
```

- **Backend URL:** [http://localhost:8080](http://localhost:8080)
- **UI Dev URL:** [http://localhost:5173](http://localhost:5173) (proxies `/api`, `/ws`, `/actuator` to backend)
- **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

### Default Credentials
```
Username: admin
Password: 123456
```

### Database Access (H2 Console)
```
JDBC URL: jdbc:h2:file:D:/Projects/vantage-master-opencode/data/vantage;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1
Username: sa
Password: vantage123
```

### First Run — Load Initial Data

Set in `application.yml`:
```yaml
init-on-fresh-db: true
```

This creates users, roles, menus, and default configurations. After first run, set to `false`.

---

## Architecture

### Project Structure

```
vantage-master-opencode/
│
├── vantage-admin/                     # SINGLE JAR — APPLICATION BOOTSTRAP (Port 8080)
│   └── src/main/java/com/pd/
│       ├── VantageAdminApplication.java  # @Modulith entry point
│       ├── gateway/                      # [SOLE EXTERNAL ENTRY POINT]
│       │   └── GatewayManagement.java    # All HTTP routes → module API interfaces
│       ├── modules/                      # [LOGICAL MODULES]
│       │   ├── system/                   # System module
│       │   │   ├── api/                  # Public API interfaces (17 services)
│       │   │   ├── service/              # Business logic + API implementations
│       │   │   ├── domain/               # Entities
│       │   │   ├── infrastructure/        # JPA repositories
│       │   │   ├── context/              # Thread-local audit context
│       │   │   ├── security/             # LoginUser, UserDetailsServiceImpl
│       │   │   ├── cache/                # Cache configuration
│       │   │   ├── listener/             # Domain event listeners
│       │   │   ├── datasource/           # Multi-datasource management
│       │   │   ├── report/               # Report designer & execution
│       │   │   └── report/api/           # ReportDesignerService
│       │   ├── quartz/                   # Quartz scheduling module
│       │   │   ├── api/                  # Public API interfaces (9 services)
│       │   │   ├── service/              # Job scheduling, email, metrics
│       │   │   ├── infrastructure/        # JPA repositories
│       │   │   └── listener/             # Job event listeners
│       │   └── generator/                # Code generation module
│       │       ├── api/                  # Public API interface
│       │       ├── service/              # Code generation logic
│       │       └── infrastructure/        # JPA repositories
│       ├── common/                       # [SHARED KERNEL]
│       │   ├── annotation/               # @Log, @RateLimit
│       │   ├── aspect/                   # AOP aspects
│       │   ├── core/                     # BaseController, AjaxResult
│       │   ├── event/                    # Domain events
│       │   ├── exception/                # Global exception handling
│       │   └── util/                     # Utility classes
│       └── framework/                    # [INFRASTRUCTURE]
│           ├── ai/                       # LangChain4j AI config & services
│           ├── config/                   # HttpExchanges, RequestLogging, WebSocket
│           └── security/                 # SecurityConfig
│
└── vantage-ui/                          # React SPA (Separate Dev Server)
    ├── src/
    │   ├── components/
    │   ├── pages/
    │   ├── themes/
    │   └── services/
    └── vite.config.js
```

### Module Initialization

Data initialization is controlled by a single flag:

```yaml
init-on-fresh-db: true   # Load initial data on startup (set false after first run)
```

---

## Features

### 1. User Management
**Endpoint:** `GET/POST/PUT/DELETE /api/system/user`

- Create, update, delete users with BCrypt-hashed passwords
- Assign roles to users
- Track login history and operation logs
- Soft delete support (delFlag)
- User audit trail via domain events

### 2. Role Management
**Endpoint:** `GET/POST/PUT/DELETE /api/system/role`

- Create/edit/delete roles with unique roleKey
- Assign menu permissions via tree view
- Data scope configuration (all, custom, dept, self)
- Admin role gets all permissions automatically

### 3. Menu Management
**Endpoint:** `GET/POST/PUT/DELETE /api/system/menu`

- Three menu types:
  - **M (Directory)** — Container for submenus
  - **C (Menu)** — Clickable page with route
  - **F (Button)** — Permission-only item
- Tree structure with parent/child relationships
- Visibility control (visible/hidden)
- Permission identifiers (e.g., `system:user:list`)

### 4. Job Scheduling (Quartz)
**Endpoints:** `GET/POST/PUT/DELETE /api/system/job`

- Cron-based scheduling with validation
- Retry mechanism (configurable count & interval)
- Timeout protection per job
- Email/Webhook notifications on failure
- Job dependencies (dependent_job_ids)
- Import/Export (JSON)
- Bulk operations (pause/resume/run)
- External webhook trigger: `POST /api/public/job/webhook/{jobId}?token={token}`

### 5. Report Management
**Endpoints:** `GET/POST/PUT/DELETE /api/system/report`

- Custom SQL report creation
- Parameter substitution (`:paramName`)
- Execute on-demand with JSON parameters
- Download as Excel/CSV
- Email with attachment
- Schedule with cron
- Dynamic system variables: `${SYSDATE}`, `${YEAR}`, `${PREV_DAY}`, etc.

### 6. Code Generation
**Endpoints:** `GET /api/tool/gen/db/tables`, `POST /api/tool/gen/batch`, `GET /api/tool/gen/download`

- Select tables from database
- Configure package, author, module name
- Generate Entity/Repository/Service/Controller + React UI
- Preview before generating
- Download as ZIP

### 7. Email Configuration
**Endpoints:** `GET/POST /api/system/email-config`, `POST /api/system/email-config/test`

- SMTP settings stored in `sys_config` table (runtime, no restart needed)
- Supports Gmail, Office 365, custom SMTP
- DynamicMailSenderService with caching

### 8. AI Chat Assistant
**Endpoints:** `POST /api/chat`, `POST /api/chat/stream` (SSE), `GET /api/chat/status`

- AI-powered responses via Ollama (Qwen2.5-Coder)
- RAG knowledge base with vector embeddings
- Streaming responses (SSE)
- Per-user conversation memory
- Knowledge base management and refresh

### 9. Monitoring Dashboard
**Path:** `/monitoring` (React page)

- System Overview: JVM memory, uptime, thread counts
- HTTP Traffic: Live request log (excludes `/actuator` calls)
- Thread Analysis: Thread state breakdown
- Health Checks: DB, disk, ping, custom components
- Actuator polling on 5–10s intervals

### 10. Script Job Execution
**Endpoint:** `POST /api/system/scriptJob/run`

- Execute SQL, JavaScript, Groovy, or Python scripts on-demand
- SQL results displayed with row count
- Script output captured and returned

---

## API Reference

### Authentication

**Login** (Spring Security formLogin, session-based)
```http
POST /api/login
Content-Type: application/x-www-form-urlencoded

username=admin&password=123456
```

**Current User**
```http
GET /api/me
```

**Logout**
```http
POST /api/logout
```

### System Module (`/api/system/...`)

| Feature | Method | Endpoint |
|---------|--------|----------|
| **Users** | GET | `/api/system/user/list` |
| | GET | `/api/system/user/{userId}` |
| | POST | `/api/system/user` |
| | PUT | `/api/system/user` |
| | DELETE | `/api/system/user/{userId}` |
| **Roles** | GET | `/api/system/role/list` |
| | GET | `/api/system/role/{roleId}` |
| | POST | `/api/system/role` |
| | PUT | `/api/system/role` |
| | DELETE | `/api/system/role/{roleId}` |
| **Menus** | GET | `/api/system/menu/tree` |
| | GET | `/api/system/menu/list` |
| | GET | `/api/system/menu/{menuId}` |
| | POST | `/api/system/menu` |
| | PUT | `/api/system/menu` |
| | DELETE | `/api/system/menu/{menuId}` |
| **Config** | GET | `/api/system/config/list` |
| | GET | `/api/system/config/key/{configKey}` |
| | PUT | `/api/system/config` |
| | POST | `/api/system/config/batch` |
| | DELETE | `/api/system/config/{configId}` |
| **Dict Types** | GET | `/api/system/dict/type/list` |
| | GET | `/api/system/dict/type/{dictId}` |
| | POST | `/api/system/dict/type` |
| | PUT | `/api/system/dict/type` |
| | DELETE | `/api/system/dict/type/{dictId}` |
| **Dict Data** | GET | `/api/system/dict/data/list?dictType=xxx` |
| | GET | `/api/system/dict/data/type/{dictType}` |
| | POST | `/api/system/dict/data` |
| | PUT | `/api/system/dict/data` |
| | DELETE | `/api/system/dict/data/{dictCode}` |
| **Notices** | GET | `/api/system/notice/list` |
| | POST | `/api/system/notice` |
| | PUT | `/api/system/notice` |
| | DELETE | `/api/system/notice/{noticeId}` |
| **Oper Logs** | GET | `/api/system/operlog/list` |
| | DELETE | `/api/system/operlog` |
| | DELETE | `/api/system/operlog/clean` |
| **Login Info** | GET | `/api/system/logininfor/list` |
| | DELETE | `/api/system/logininfor` |
| | DELETE | `/api/system/logininfor/clean` |
| **Email Config** | GET | `/api/system/email-config` |
| | POST | `/api/system/email-config` |
| | POST | `/api/system/email-config/test` |
| **Notifications** | GET | `/api/system/notifications/list` |
| | GET | `/api/system/notifications/unread` |
| | PUT | `/api/system/notifications/read-all` |
| **Datasource** | GET | `/api/system/datasource/list` |
| | POST | `/api/system/datasource` |
| | PUT | `/api/system/datasource` |
| | DELETE | `/api/system/datasource/{datasourceId}` |
| | POST | `/api/system/datasource/test` |
| **Cache** | GET | `/api/system/cache/list` |
| | POST | `/api/system/cache/clear/{cacheName}` |
| | POST | `/api/system/cache/clear-all` |

### Quartz Module (`/api/system/job`, `/api/system/job-log`, etc.)

| Feature | Method | Endpoint |
|---------|--------|----------|
| **Jobs** | GET | `/api/system/job/list` |
| | GET | `/api/system/job/{jobId}` |
| | POST | `/api/system/job` |
| | PUT | `/api/system/job` |
| | DELETE | `/api/system/job/{jobId}` |
| | PUT | `/api/system/job/changeStatus` |
| | POST | `/api/system/job/run` |
| | PUT | `/api/system/job/pause` |
| | PUT | `/api/system/job/resume` |
| | GET | `/api/system/job/export` |
| | POST | `/api/system/job/import` |
| | GET | `/api/system/job/groups` |
| **Job Logs** | GET | `/api/system/job-log/list` |
| | DELETE | `/api/system/job-log/{logId}` |
| | DELETE | `/api/system/job-log/batch` |
| | DELETE | `/api/system/job-log/clean` |
| **Job Templates** | GET | `/api/system/job-template/list` |
| | GET | `/api/system/job-template/{name}` |
| | POST | `/api/system/job-template/create/{name}` |
| **Email Templates** | GET | `/api/system/email-template/list` |
| | GET | `/api/system/email-template/active` |
| | GET | `/api/system/email-template/{templateId}` |
| | POST | `/api/system/email-template` |
| | PUT | `/api/system/email-template` |
| | DELETE | `/api/system/email-template/{templateId}` |
| | POST | `/api/system/email-template/preview` |
| **Job Groups** | GET | `/api/system/job-group/list` |
| | GET | `/api/system/job-group/{group}/jobs` |
| | POST | `/api/system/job-group/{group}/execute` |
| **Job Dashboard** | GET | `/api/system/job-dashboard/metrics` |
| | GET | `/api/system/job-dashboard/trend` |
| | GET | `/api/system/job-dashboard/health` |
| **Script Jobs** | POST | `/api/system/scriptJob/run` |
| **Webhook** | POST | `/api/public/job/webhook/{jobId}?token=xxx` |

### Report Module (`/api/system/report`)

| Feature | Method | Endpoint |
|---------|--------|----------|
| **Reports** | GET | `/api/system/report/list` |
| | GET | `/api/system/report/{reportId}` |
| | POST | `/api/system/report` |
| | PUT | `/api/system/report` |
| | DELETE | `/api/system/report/{reportId}` |
| | POST | `/api/system/report/execute/{reportId}` |
| | GET | `/api/system/report/templates` |
| | POST | `/api/system/report/from-template` |
| | POST | `/api/system/report/schedule/{templateId}` |
| | DELETE | `/api/system/report/unschedule/{reportId}` |

### Generator Module (`/api/tool/gen`)

| Feature | Method | Endpoint |
|---------|--------|----------|
| **DB Tables** | GET | `/api/tool/gen/db/tables` |
| **Preview** | GET | `/api/tool/gen/preview?table=xxx` |
| **Batch Gen** | POST | `/api/tool/gen/batch` |
| **Download** | GET | `/api/tool/gen/download?tables=xxx` |

### AI Chat (`/api/chat`)

| Feature | Method | Endpoint |
|---------|--------|----------|
| **Chat** | POST | `/api/chat` |
| **Stream** | POST | `/api/chat/stream` (SSE) |
| **Status** | GET | `/api/chat/status` |
| **History** | GET | `/api/chat/history` |
| **Clear** | POST | `/api/chat/clear-memory` |
| **Refresh KB** | POST | `/api/chat/knowledge/refresh` |
| **KB Stats** | GET | `/api/chat/knowledge/stats` |

---

## Database Schema

### Core Tables

| Table | Purpose |
|-------|---------|
| `sys_user` | User accounts with BCrypt passwords |
| `sys_role` | Roles with permissions and data scope |
| `sys_menu` | Navigation menus (M/C/F types) |
| `sys_user_role` | User-role assignments |
| `sys_role_menu` | Role-menu permission assignments |
| `sys_config` | System configuration key-value store |
| `sys_dict_type` | Dictionary type definitions |
| `sys_dict_data` | Dictionary data entries |
| `sys_notice` | System announcements |
| `sys_oper_log` | Operation audit logs |
| `sys_logininfor` | Login history |
| `sys_job` | Quartz scheduled jobs |
| `sys_job_log` | Job execution logs |
| `sys_report` | Report definitions |
| `sys_report_template` | Report templates |
| `sys_email_template` | Email templates with SQL data tables |
| `sys_datasource` | Database datasource definitions |
| `sys_notification` | In-app notifications |
| `ai_knowledge` | AI knowledge base documents (RAG) |
| `gen_table` | Code generation table configurations |
| `gen_table_column` | Code generation column configurations |
| `qrtz_*` | Quartz scheduler internal tables |

---

## Spring Boot Actuator

### Available Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health (show-details: always) |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus-compatible metrics |
| `/actuator/modulith` | Spring Modulith module metadata |
| `/actuator/httpexchanges` | HTTP request/response log (in-memory, capacity 10000) |
| `/actuator/loggers` | Logger level configuration |
| `/actuator/env` | Environment properties |
| `/actuator/threaddump` | JVM thread dump |
| `/actuator/heapdump` | JVM heap dump |
| `/actuator/caches` | Cache manager info |
| `/actuator/scheduledtasks` | Scheduled tasks list |
| `/actuator/beans` | Spring bean definitions |
| `/actuator/configprops` | Configuration properties |
| `/actuator/logfile` | Application log file |

### Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,logfile,loggers,httpexchanges,env,threaddump,heapdump,caches,scheduledtasks,configprops,beans,modulith
  endpoint:
    health:
      show-details: always
    modulith:
      enabled: true
```

---

## Spring Modulith Architecture

Vantage is a **Modular Monolith** using `@Modulith`. All modules run on a single port (8080) with clear boundaries enforced by Spring Modulith's module detection.

### Module Dependency Graph

```
┌─────────────────────────────────────────────────────┐
│                  vantage-admin                       │
│  ┌───────────────────────────────────────────────┐  │
│  │  GatewayManagement (sole @RestController)     │  │
│  │  Routes all 80+ endpoints through API layers  │  │
│  └────────────┬──────────────┬──────────────┬────┘  │
│               │              │              │        │
│  ┌────────────▼─────┐ ┌──────▼──────┐ ┌────▼──────┐ │
│  │  system module   │ │quartz module│ │generator  │ │
│  │  17 API services │ │9 API services│ │1 API svc  │ │
│  └────────┬─────────┘ └──────┬──────┘ └────┬──────┘ │
│           │                  │              │        │
│  ┌────────▼──────────────────▼──────────────▼──────┐ │
│  │                  common (shared kernel)          │ │
│  └──────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────┘
```

### Access Rules

1. **GatewayManagement** → imports only `module.api.*` packages
2. **Module services** → import only `common.*` packages
3. **Module domains** → never cross module boundaries directly
4. **Cross-module communication** → only through public API interfaces

### API Interfaces (27 total)

| Module | Interface | Purpose |
|--------|-----------|---------|
| System | `SystemAuthService` | Authentication operations |
| System | `SystemUserService` | User CRUD |
| System | `SystemRoleService` | Role management |
| System | `SystemMenuService` | Menu tree & permissions |
| System | `SystemDictService` | Dictionary types & data |
| System | `SystemConfigService` | System config |
| System | `SystemNoticeService` | Announcements |
| System | `SystemNotificationService` | In-app notifications |
| System | `SystemOperLogService` | Operation audit logs |
| System | `SystemLogininforService` | Login history |
| System | `SystemDatasourceService` | Multi-datasource management |
| System | `SystemEmailConfigService` | Email SMTP configuration |
| System | `SystemCacheService` | Cache management |
| System | `SystemChatService` | AI chat operations |
| System | `SystemReportEntityService` | Report CRUD & execution |
| System | `SystemReportDesignerService` | Report designer |
| System | `SystemReportService` | Report service |
| System | `SystemMailService` | Mail sender operations |
| Quartz | `QuartzJobService` | Job CRUD & scheduling |
| Quartz | `QuartzJobLogService` | Job execution logs |
| Quartz | `QuartzJobTemplateService` | Job templates |
| Quartz | `QuartzEmailJobTemplateService` | Email job templates |
| Quartz | `QuartzEmailTemplateService` | Email template service |
| Quartz | `QuartzJobGroupService` | Job group operations |
| Quartz | `QuartzJobMetricsService` | Job metrics & health |
| Quartz | `QuartzScriptJobService` | Script execution |
| Quartz | `QuartzJobWebhookService` | Webhook triggers |
| Generator | `GeneratorService` | Code generation |

---

## Gateway Pattern

### Design Philosophy

Vantage follows the **piomin/sample-spring-modulith** pattern: a single `GatewayManagement` controller handles all HTTP routing, delegating to module API interfaces. No module controller is directly exposed.

### Benefits

- **Single audit point** — all external routes defined in one place
- **True module isolation** — only API interfaces are visible across modules
- **Extraction-ready** — modules can be split into separate JARs later
- **No HTTP coupling** — modules are unaware of REST annotations

### Request Flow

```
HTTP Request → GatewayManagement → API Interface → Service Impl → Repository → Database
```

### GatewayManagement Structure

The `GatewayManagement` controller (~900 lines) is organized by module:

1. **Auth** (`/api/me`, `/api/logout`)
2. **System: User** (`/api/system/user/*`)
3. **System: Role** (`/api/system/role/*`)
4. **System: Menu** (`/api/system/menu/*`)
5. **System: Dict** (`/api/system/dict/*`)
6. **System: Config** (`/api/system/config/*`)
7. **System: OperLog** (`/api/system/operlog/*`)
8. **System: Logininfor** (`/api/system/logininfor/*`)
9. **System: Notice** (`/api/system/notice/*`)
10. **System: Notifications** (`/api/system/notifications/*`)
11. **System: Datasource** (`/api/system/datasource/*`)
12. **System: Email Config** (`/api/system/email-config/*`)
13. **System: Cache** (`/api/system/cache/*`)
14. **System: Chat** (`/api/chat/*`)
15. **System: Report** (`/api/system/report/*`)
16. **Quartz: Job** (`/api/system/job/*`)
17. **Quartz: Job Log** (`/api/system/job-log/*`)
18. **Quartz: Job Template** (`/api/system/job-template/*`)
19. **Quartz: Email Template** (`/api/system/email-template/*`)
20. **Quartz: Job Group** (`/api/system/job-group/*`)
21. **Quartz: Job Dashboard** (`/api/system/job-dashboard/*`)
22. **Quartz: Script Job** (`/api/system/scriptJob/*`)
23. **Quartz: Job Webhook** (`/api/public/job/webhook/*`)
24. **Generator** (`/api/tool/gen/*`)

---

## Testing Strategy

### Test Classes

| Class | Coverage |
|-------|----------|
| `SystemApiTests` | Health, info, metrics, prometheus, modulith, login, chat endpoints |
| `QuartzApiTests` | Job list, groups, logs, dashboard health/trend |
| `GeneratorApiTests` | DB tables discovery, generation list |
| `ModulithTests` | Module boundary verification + documentation generation |

### Running Tests

```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=SystemApiTests

# Verify module boundaries
mvn test -Dtest=ModulithTests#verifyModuleStructure

# Generate modulith docs (PlantUML + Markdown)
mvn test -Dtest=ModulithTests#generateDocumentation

# Skip tests
mvn test -DskipTests
```

### Example Test

```java
@SpringBootTest
@AutoConfigureMockMvc
class SystemApiTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }
}
```

---

## Troubleshooting

### Application Won't Start
**Port 8080 already in use:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /F /PID <PID>
```

### Login Fails
**Admin user not found:**
```sql
SELECT * FROM sys_user WHERE login_name='admin';
-- If missing, delete data/vantage.mv.db and restart with init-on-fresh-db: true
```

### Jobs Not Executing
- Check logs for: "Initializing scheduled jobs"
- Verify job status = '0' (active)
- Verify cron expression is valid

### Report Execution Fails
- Check parameter format (`:paramName`)
- Verify table/column names exist
- Test SQL in H2 Console first

### Code Generation Fails
- Verify database connection works
- Check table exists in PUBLIC schema
- Test: `GET /api/tool/gen/db/tables`

### AI Chat Not Responding
- Verify Ollama is running: `http://localhost:11434`
- Check `ai.enabled: true` in application.yml
- Verify model is pulled: `ollama pull qwen2.5-coder:0.5b`

---

## Configuration Reference

### application.yml Key Settings

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:D:/Projects/vantage-master-opencode/data/vantage
    username: sa
    password: vantage123
  h2.console:
    enabled: true
    path: /h2-console
  jpa.hibernate:
    ddl-auto: update

init-on-fresh-db: false    # Set true only on first run

ai:
  enabled: true
  ollama-base-url: http://localhost:11434
  chat-model: qwen2.5-coder:0.5b
  rag-enabled: true
  timeout: 60
```

### Switching Databases

**PostgreSQL:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vantage
    driverClassName: org.postgresql.Driver
    username: postgres
    password: yourpassword
```

**MySQL:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vantage
    driverClassName: com.mysql.cj.jdbc.Driver
    username: root
    password: yourpassword
```

---

**Version:** 2.0.0  
**Last Updated:** 2026-05-01  
**Build:** Spring Boot 4.0.5 + Spring Modulith 2.0.5 + React 19  
**Architecture:** Single JAR Modulith with Gateway-Only Routing
