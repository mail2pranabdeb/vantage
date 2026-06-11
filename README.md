# Vantage Admin Platform

[![CI/CD](https://github.com/mail2pranabdeb/vantage/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/mail2pranabdeb/vantage/actions/workflows/ci.yml)
[![Architecture](https://img.shields.io/badge/Architecture-Spring%20Modulith-blue.svg)](https://spring.io/projects/spring-modulith)
[![UI](https://img.shields.io/badge/UI-React%2019%20%2B%20Vite-61dafb.svg)](https://react.dev/)
[![Database](https://img.shields.io/badge/Database-Database%20Agnostic-green.svg)](https://www.h2database.com/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED.svg)](https://docker.com)
[![Version](https://img.shields.io/badge/Release-v1.0.0-blue)](https://github.com/mail2pranabdeb/vantage/releases/tag/v1.0.0)

A premium, enterprise-grade business management system built with **Spring Boot 4.0.5** and **React 19**. Featuring a state-of-the-art **Glassmorphism UI**, real-time monitoring dashboard, AI-powered chat assistant, audit trail with diff history, API rate limiting, global search, PDF export, and data import. Built as a **Spring Modulith 2.0.5** modular monolith with **gateway-only external routing** — all module communication flows through published API interfaces.

---

## ⚡ Quick Start

### Prerequisites
- **Java 17** (Required)
- **Node.js 20.11+** (Required for Vite)
- **Maven 3.8+**
- **Docker Desktop** (optional — for containerized deployment)

### Option A: Local Dev (separate backend + frontend)

```bash
# 1. Build backend
mvnw.cmd clean package -DskipTests

# 2. Run backend
cd vantage-admin
mvnw.cmd spring-boot:run

# 3. Run frontend (separate terminal)
cd vantage-ui
npm install
npm run dev
```

- **UI Dev URL**: [http://localhost:5173](http://localhost:5173) (proxies `/api`, `/ws`, `/actuator` to backend)
- **Backend URL**: [http://localhost:8080](http://localhost:8080)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **Default Credentials**: `admin` / `123456`

### Option B: Docker (self-contained)

```bash
# Start app + PostgreSQL
docker compose up -d

# App at http://localhost:8080
# All features work immediately
```

The Docker image embeds the React frontend directly in the Spring Boot JAR. No separate frontend server needed.

### First Run — Load Initial Data
Set in `vantage-admin/src/main/resources/application.yml`:
```yaml
init-on-fresh-db: true
```

After first run, set to `false`.

---

## 🏗 Architecture

### Single JAR Modulith

Vantage uses a **flattened single-JAR architecture**. All Maven modules (`vantage-common`, `vantage-modules/*`) have been merged into `vantage-admin`. Logical modules are enforced via Spring Modulith `@ApplicationModule` annotations.

```
vantage-master-opencode/
│
├── vantage-admin/                     # SINGLE JAR — APPLICATION BOOTSTRAP (Port 8080)
│   └── src/main/java/com/pd/
│       ├── VantageAdminApplication.java  # @Modulith entry point
│       ├── gateway/                      # [SOLE EXTERNAL ENTRY POINT]
│       │   └── GatewayManagement.java    # All HTTP routes → module API interfaces
│       ├── modules/                      # [LOGICAL MODULES]
│       │   ├── system/                   # System module (users, roles, menus, config, ...)
│       │   │   ├── api/                  # Public API interfaces (17 services)
│       │   │   ├── service/              # Business logic + API implementations
│       │   │   ├── domain/               # Entities (SysUser, SysRole, SysMenu, ...)
│       │   │   ├── infrastructure/        # JPA repositories
│       │   │   ├── context/              # Thread-local audit context
│       │   │   ├── security/             # LoginUser, UserDetailsServiceImpl
│       │   │   ├── cache/                # Cache configuration
│       │   │   ├── listener/             # Domain event listeners
│       │   │   ├── datasource/           # Multi-datasource management
│       │   │   ├── report/               # Report designer & execution
│       │   │   └── report/api/           # ReportDesignerService (cross-module API)
│       │   ├── quartz/                   # Quartz scheduling module
│       │   │   ├── api/                  # Public API interfaces (9 services)
│       │   │   ├── service/              # Job scheduling, email, metrics
│       │   │   ├── infrastructure/        # JPA repositories
│       │   │   └── listener/             # Job event listeners
│       │   └── generator/                # Code generation module
│       │       ├── api/                  # Public API interface
│       │       ├── service/              # Code generation logic
│       │       └── infrastructure/        # JPA repositories
│       ├── common/                       # [SHARED KERNEL] (merged from vantage-common)
│       │   ├── annotation/               # @Log, @RateLimit, etc.
│       │   ├── aspect/                   # AOP aspects (logging, audit)
│       │   ├── core/                     # BaseController, AjaxResult
│       │   ├── event/                    # Domain events (UserCreated, JobCompleted)
│       │   ├── exception/                # Global exception handling
│       │   └── util/                     # Utility classes
│       └── framework/                    # [INFRASTRUCTURE]
│           ├── ai/                       # LangChain4j AI config & services
│           ├── config/                   # HttpExchanges, RequestLogging, WebSocket
│           └── security/                 # SecurityConfig
│
└── vantage-ui/                          # React SPA (Separate Dev Server, Port 5173)
    ├── src/
    │   ├── components/                   # Sidebar, TabBar, DataGrid, Modal, etc.
    │   ├── pages/                        # All page components
    │   ├── themes/                       # Glassmorphism CSS themes
    │   └── services/                     # API service layer
    └── vite.config.js                    # Dev proxies: /api, /ws, /actuator → :8080
```

### Gateway-Only External Routing

```
HTTP Request → GatewayManagement → Module API Interface → Service Impl → Repository
```

**No module controller is directly exposed.** All external communication routes through `GatewayManagement.java` via published API interfaces only. This enforces Spring Modulith boundaries:

1. **Gateway** → **module api/** packages only (never service/ or infrastructure/)
2. **Modules** → **common** shared kernel
3. **Framework** → **common** shared kernel
4. **Cross-module** access only through **public API interfaces**

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
│  │  (users, roles,  │ │(jobs, logs, │ │(code gen) │ │
│  │   menus, config, │ │ templates,  │ │           │ │
│  │   reports, ...)  │ │ metrics)    │ │           │ │
│  └────────┬─────────┘ └──────┬──────┘ └────┬──────┘ │
│           │                  │              │        │
│  ┌────────▼──────────────────▼──────────────▼──────┐ │
│  │                  common (shared kernel)          │ │
│  │  (annotations, aspects, events, exceptions)      │ │
│  └──────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────┘
```

---

## 📦 Module Ecosystem

### 🛠 System Control Center
- **User & Role Management**: Granular RBAC permissions with BCrypt passwords
- **Dynamic Menus**: Three-level navigation tree (Directory/Menu/Button types)
- **Dictionary Management**: Centralized constants and lookup data
- **System Config**: Runtime-updatable key-value store (SMTP, notifications)
- **Multi-Datasource**: Manage and test external database connections
- **Operation & Login Audit**: Full audit trails with `@Audited` annotation, Hibernate event listeners, side-by-side diff history
- **API Rate Limiting**: `@RateLimit` annotation, token-bucket via Caffeine, per-endpoint quotas
- **Global Search / Command Palette**: Ctrl+K or `⌘K` — searches Users, Roles, Menus, Configs, Notices, Jobs
- **Export Engine**: PDF (OpenPDF), CSV, Excel exports on all list pages
- **Data Import Wizard**: 3-step CSV/Excel upload → preview → execute

### ⏱ Advanced Job Scheduling (Quartz)
- **Execution Controls**: Configurable retries, timeouts, and job dependencies
- **Notifications**: Email/Webhook alerts on failure
- **Job Groups**: Execute all jobs in a group sequentially
- **Webhook Triggers**: External job execution with per-job tokens
- **Script Jobs**: Execute SQL, JavaScript, Groovy, Python scripts on-demand
- **Email Templates**: Dynamic SQL data tables rendered in email body
- **Dashboard**: Health monitoring, execution trends, metrics

### 📊 Advanced Reporting Module
- **SQL Reports**: Custom SQL with parameter substitution (`:paramName`)
- **Visual Designer**: Report template creation with datasource selection
- **Dynamic System Variables**: `${SYSDATE}`, `${YEAR}`, `${PREV_DAY}`, etc.
- **Multi-Output**: Excel, CSV download
- **Scheduled Delivery**: Cron-based report execution with email attachment
- **Template-Based**: Create reports from existing templates

### 🤖 AI Chat Assistant
- **LLM Integration**: LangChain4j with Ollama (local LLM)
- **RAG Knowledge Base**: 12 document categories with vector embeddings
- **Streaming Responses**: SSE-based real-time token streaming
- **Per-User Memory**: Isolated conversation history per user
- **Knowledge Management**: Refresh, stats, and document loading

### 📈 Real-time Monitoring Dashboard
- **System Overview**: JVM memory, uptime, thread counts, DB pool status
- **HTTP Traffic**: Live request log (excludes `/actuator` calls)
- **Thread Analysis**: Thread state breakdown and top busy threads
- **Health Checks**: DB, disk, ping, custom component health indicators
- **Actuator Polling**: 5–10s intervals for real-time data

### 🔧 Code Generation
- **Database Introspection**: Auto-discover tables and columns
- **Full CRUD Generation**: Entity, Repository, Service, Controller + React UI
- **Preview & Download**: Review code before downloading as ZIP

---

## 🛠 Technical Stack

### Backend
| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 4.0.5 | Core framework |
| Spring Modulith | 2.0.5 | Module encapsulation & verification |
| Spring Security | — | Session-based authentication (BCrypt) |
| Spring Data JPA | — | Database-agnostic persistence |
| Quartz | — | Enterprise job scheduling |
| LangChain4j | 0.35.0 | AI/LLM integration (Ollama) |
| Spring Boot Actuator | — | Health, metrics, module monitoring |
| Lombok | — | Reduced boilerplate |
| H2 Database | — | Embedded file-based database |

### Frontend
| Component | Version | Purpose |
|-----------|---------|---------|
| React | 19 | Component architecture |
| Vite | 5 | Build tool with dev proxies |
| Glassmorphism CSS | — | Custom design system |
| Lucide Icons | — | Icon library |

### Development Notes
- **JDK 17 Required** — no Java 21+ features used
- **UI is separate in dev, embedded in Docker** — Vite dev server proxies to backend; Docker image bundles frontend in JAR static resources
- **Gateway pattern** — `GatewayManagement` is the only `@RestController`
- **API-first** — modules expose only `api/` interfaces externally

---

## 📡 API Reference

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/login` | Session-based login (formLogin) |
| GET | `/api/me` | Current authenticated user |
| POST | `/api/logout` | Logout |

### System Module

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/user/list` | List active users |
| GET | `/api/system/user/{userId}` | Get user by ID |
| POST | `/api/system/user` | Create user |
| PUT | `/api/system/user` | Update user |
| DELETE | `/api/system/user/{userId}` | Delete user |
| GET | `/api/system/role/list` | List active roles |
| GET | `/api/system/role/{roleId}` | Get role by ID |
| POST | `/api/system/role` | Create role |
| PUT | `/api/system/role` | Update role |
| DELETE | `/api/system/role/{roleId}` | Delete role |
| GET | `/api/system/menu/tree` | Get menu tree |
| GET | `/api/system/menu/list` | List all menus |
| POST | `/api/system/menu` | Create menu |
| PUT | `/api/system/menu` | Update menu |
| DELETE | `/api/system/menu/{menuId}` | Delete menu |
| GET | `/api/system/dict/type/list` | List dictionary types |
| GET | `/api/system/dict/data/list?dictType=xxx` | Get dict data by type |
| POST | `/api/system/dict/type` | Create dict type |
| POST | `/api/system/dict/data` | Create dict data |
| GET | `/api/system/config/list` | List all configs |
| PUT | `/api/system/config` | Update config |
| POST | `/api/system/config/batch` | Batch save configs |
| GET | `/api/system/datasource/list` | List datasources |
| POST | `/api/system/datasource` | Create datasource |
| POST | `/api/system/datasource/test` | Test datasource connection |
| GET | `/api/system/notifications/list` | User notifications |
| PUT | `/api/system/notifications/read-all` | Mark all as read |
| GET | `/api/system/operlog/list` | Operation audit logs (with diff details) |
| GET | `/api/system/operlog/{operId}` | Operation log detail (old/new values) |
| DELETE | `/api/system/operlog` | Delete operation logs |
| DELETE | `/api/system/operlog/clean` | Clean all operation logs |
| GET | `/api/system/logininfor/list` | Login history |
| GET | `/api/system/notice/list` | System notices |
| GET | `/api/system/search?q=xxx` | Global search (Ctrl+K) |
| POST | `/api/system/export` | Export data (PDF/CSV/Excel) |
| POST | `/api/system/import/preview` | Preview import file |
| POST | `/api/system/import/execute` | Execute import |
| GET | `/api/system/rate-limit/stats` | Rate limiting statistics |
| GET | `/api/system/email-config` | Email configuration |
| POST | `/api/system/email-config` | Save email config |
| POST | `/api/system/email-config/test` | Send test email |
| GET | `/api/system/cache/list` | Cache statistics |
| POST | `/api/system/cache/clear/{cacheName}` | Clear specific cache |

### Quartz Module

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/job/list` | List all jobs |
| GET | `/api/system/job/{jobId}` | Get job by ID |
| POST | `/api/system/job` | Create job |
| PUT | `/api/system/job` | Update job |
| DELETE | `/api/system/job/{jobId}` | Delete job |
| POST | `/api/system/job/run` | Run job immediately |
| PUT | `/api/system/job/pause` | Pause job |
| PUT | `/api/system/job/resume` | Resume job |
| GET | `/api/system/job/groups` | List job groups |
| GET | `/api/system/job/export` | Export jobs (JSON) |
| POST | `/api/system/job/import` | Import jobs (JSON) |
| GET | `/api/system/job-log/list` | List job logs |
| DELETE | `/api/system/job-log/clean` | Clear all job logs |
| GET | `/api/system/job-template/list` | List job templates |
| POST | `/api/system/job-template/create/{name}` | Create job from template |
| GET | `/api/system/email-template/list` | List email templates |
| POST | `/api/system/email-template/preview` | Preview email template |
| GET | `/api/system/job-group/list` | Job group summary |
| POST | `/api/system/job-group/{group}/execute` | Execute all jobs in group |
| GET | `/api/system/job-dashboard/metrics` | Dashboard metrics |
| GET | `/api/system/job-dashboard/trend` | Execution trend (30 days) |
| GET | `/api/system/job-dashboard/health` | Job health status |
| POST | `/api/system/scriptJob/run` | Execute script (SQL/JS/Groovy/Python) |
| POST | `/api/public/job/webhook/{jobId}` | External webhook trigger |

### Report Module

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/report/list` | List all reports |
| POST | `/api/system/report` | Create report |
| PUT | `/api/system/report` | Update report |
| DELETE | `/api/system/report/{reportId}` | Delete report |
| POST | `/api/system/report/execute/{reportId}` | Execute report |
| GET | `/api/system/report/templates` | List report templates |
| POST | `/api/system/report/from-template` | Create from template |
| POST | `/api/system/report/schedule/{templateId}` | Schedule report |

### Generator Module

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tool/gen/db/tables` | List database tables |
| GET | `/api/tool/gen/preview` | Preview generated code |
| POST | `/api/tool/gen/batch` | Batch generate code |
| GET | `/api/tool/gen/download` | Download code as ZIP |

### AI Chat

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chat` | Send message (blocking) |
| POST | `/api/chat/stream` | Send message (SSE streaming) |
| GET | `/api/chat/status` | AI service status |
| GET | `/api/chat/history` | Conversation history |
| POST | `/api/chat/clear-memory` | Clear conversation memory |
| POST | `/api/chat/knowledge/refresh` | Refresh knowledge base |
| GET | `/api/chat/knowledge/stats` | Knowledge base statistics |

### Actuator

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health (show-details: always) |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus-compatible metrics |
| `/actuator/modulith` | Spring Modulith module metadata |
| `/actuator/httpexchanges` | HTTP request/response log |
| `/actuator/loggers` | Logger level configuration |
| `/actuator/threaddump` | JVM thread dump |
| `/actuator/heapdump` | JVM heap dump |
| `/actuator/caches` | Cache manager info |

---

## 🗄 Database Schema

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

## 🔒 Module Access Rules

1. **GatewayManagement** → imports only `module.api.*` packages
2. **Module services** → import only `common.*` packages
3. **Module domains** → never cross module boundaries directly
4. **Cross-module communication** → only through public API interfaces
5. **No module controllers** — all HTTP routing centralized in gateway

### API Interface Summary (27 total)

| Module | Interfaces |
|--------|-----------|
| **System** | `SystemAuthService`, `SystemUserService`, `SystemRoleService`, `SystemMenuService`, `SystemDictService`, `SystemConfigService`, `SystemNoticeService`, `SystemNotificationService`, `SystemOperLogService`, `SystemLogininforService`, `SystemDatasourceService`, `SystemEmailConfigService`, `SystemCacheService`, `SystemChatService`, `SystemReportEntityService`, `SystemReportDesignerService`, `SystemReportService`, `SystemMailService` |
| **Quartz** | `QuartzJobService`, `QuartzJobLogService`, `QuartzJobTemplateService`, `QuartzEmailJobTemplateService`, `QuartzEmailTemplateService`, `QuartzJobGroupService`, `QuartzJobMetricsService`, `QuartzScriptJobService`, `QuartzJobWebhookService` |
| **Generator** | `GeneratorService` |

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=SystemApiTests
mvn test -Dtest=QuartzApiTests
mvn test -Dtest=GeneratorApiTests

# Verify module boundaries
mvn test -Dtest=ModulithTests#verifyModuleStructure

# Generate modulith documentation
mvn test -Dtest=ModulithTests#generateDocumentation

# Skip tests
mvn clean package -DskipTests
```

### Test Classes

| Class | Coverage |
|-------|----------|
| `SystemApiTests` | Health, info, metrics, prometheus, modulith, login, chat endpoints |
| `QuartzApiTests` | Job list, groups, logs, dashboard health/trend |
| `GeneratorApiTests` | DB tables discovery, generation list |
| `ModulithTests` | Module boundary verification + documentation generation |

---

## 🔧 Configuration

### application.yml Key Settings

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:D:/Projects/vantage-master-opencode/data/vantage;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1
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

## 🐛 Troubleshooting

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

### Docker Container Won't Start
**Check logs:**
```bash
docker compose logs vantage
docker compose logs postgres
```
**Port conflict:** Ensure port 8080 or 5432 isn't already in use:
```bash
netstat -ano | findstr :8080
```

### Docker Hub Push Fails
Ensure GitHub secrets `DOCKER_USERNAME` and `DOCKER_PASSWORD` are configured in the repository settings under `Settings → Secrets and variables → Actions`.

### AI Chat Not Responding
- Verify Ollama is running: `http://localhost:11434`
- Check `ai.enabled: true` in application.yml
- Verify model is pulled: `ollama pull qwen2.5-coder:0.5b`

---

## ✨ Features (2026-06)

### Audit Trail (v1.0.0)
- `@Audited` annotation for opt-in entity tracking
- Hibernate `PreUpdateEventListener` / `PreDeleteEventListener` — zero extra DB queries
- Maps old/new values with `EntityDiffUtil` for side-by-side UI rendering
- 3-tier fallback: Hibernate state → `UserAuditContextHolder` → AOP arguments
- 8 annotated entities, auto-logged for all GatewayManagement write methods

### Rate Limiting (v1.0.0)
- `@RateLimit` annotation with configurable capacity / refill rate
- Token-bucket via Caffeine (no Redis dependency)
- Stats endpoint with per-bucket metrics
- `X-RateLimit-*` response headers; HTTP 429 with `Retry-After`

### Global Search / Command Palette (v1.0.0)
- Ctrl+K / `⌘K` keyboard shortcut
- Unified search across 6 entity types + static nav commands
- Debounced API calls (300ms), permission-filtered results

### Export Engine (v1.0.0)
- PDF (OpenPDF), CSV, Excel via generic `ExportService`
- Export buttons on all 10 list pages + Report Designer
- Hardcoded filenames on frontend (no Content-Disposition parsing)

### Data Import Wizard (v1.0.0)
- 3-step: Upload → Preview → Results
- CSV (Commons CSV) and Excel (Apache POI) support
- All rows returned from backend; preview capped client-side

### Docker & CI/CD (v1.0.0)
- Multi-stage Dockerfile (Node 20 → JDK 17 → JRE 17)
- Docker Compose with PostgreSQL for Desktop local dev
- GitHub Actions: build, test, push to Docker Hub, SSH deploy

---

## 🚀 Roadmap

- [ ] **Tool Execution**: Implement LangChain4j `@Tool` for actual AI-driven system operations
- [ ] **Conversation Persistence**: Store chat history in database
- [x] **PDF Report Export**: Integrated PDF generation with OpenPDF
- [x] **CSV/Excel Data Import**: 3-step import wizard with preview
- [x] **Docker & CI/CD**: Multi-stage build, GitHub Actions, Docker Hub push
- [ ] **Metric Charting**: Historical traffic/health trends with Recharts
- [ ] **Gantt Timeline**: Visualizer for job dependency chains
- [ ] **Module Extraction**: Optional separate JARs for system/quartz/generator

---

**Version:** 2.1.0  
**Last Updated:** 2026-06-11  
**Build:** Spring Boot 4.0.5 + Spring Modulith 2.0.5 + React 19  
**Architecture:** Single JAR Modulith with Gateway-Only Routing  
**Container:** Docker multi-stage build + PostgreSQL + GitHub Actions CI/CD
