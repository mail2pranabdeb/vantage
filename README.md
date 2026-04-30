# Vantage Admin Platform

[![Architecture](https://img.shields.io/badge/Architecture-Spring%20Modulith-blue.svg)](https://spring.io/projects/spring-modulith)
[![UI](https://img.shields.io/badge/UI-React%2019%20%2B%20Vite-61dafb.svg)](https://react.dev/)
[![Database](https://img.shields.io/badge/Database-Database%20Agnostic-green.svg)](https://www.h2database.com/)

A premium, enterprise-grade business management system built with **Spring Boot 4.0.5** and **React 19**. Featuring a state-of-the-art **Glassmorphism UI**, real-time monitoring dashboard, and a unified technical control center. Built as a **Spring Modulith 2.0.5** modular monolith running on a single port **8080**.

---

## ⚡ Quick Start

### Prerequisites
- **Java 17** (Required)
- **Node.js 20.11+** (Required for Vite)
- **Maven 3.8+**

### 1. Build
```bash
# Windows
mvnw.cmd clean compile

# Linux/macOS
./mvnw clean compile
```

### 2. Run Backend
```bash
cd vantage-admin
mvnw.cmd spring-boot:run
```

### 3. Run UI (Development)
```bash
cd vantage-ui
npm install
npm run dev
```

- **UI Dev URL**: [http://localhost:5173](http://localhost:5173) (proxies `/api`, `/ws`, `/actuator` to backend)
- **App URL**: [http://localhost:8080](http://localhost:8080)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **Default Credentials**: `admin` / `123456`

### First Run - Load Initial Data
To load initial data (users, roles, menus), set in `application.yml`:
```yaml
init-on-fresh-db: true
```

After first run, set to `false` to skip data initialization:
```yaml
init-on-fresh-db: false
```

---

## 🛠 Configuration

### application.yml
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:D:/Projects/vantage-master-opencode/data/vantage;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1
    driverClassName: org.h2.Driver
    username: sa
    password: vantage123
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: update  # create, create-drop, update, validate
    show-sql: false

# Data initialization - set to true to load initial data (first run only)
init-on-fresh-db: false
```

### Switching Databases
The application is database-agnostic. To switch databases:

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

**Oracle:**
```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:vantage
    driverClassName: oracle.jdbc.OracleDriver
    username: system
    password: yourpassword
```

---

## 📦 Project Structure (Spring Modulith)

```
vantage-master-opencode/
│
├── vantage-common/                    # Shared Kernel (cross-cutting concerns)
│   └── src/main/java/com/pd/common/
│       ├── annotation/                # Custom annotations (@Log, etc.)
│       ├── aspect/                    # AOP aspects
│       ├── core/                      # Base classes (BaseController, AjaxResult)
│       ├── event/                     # Domain event utilities
│       ├── exception/                 # Exception handling
│       └── util/                      # Utility classes
│
├── vantage-modules/                  # Business Modules (Logical Modules)
│   │
│   ├── bms-module-system/            # [SYSTEM MODULE]
│   │   └── src/main/java/com/pd/modules/system/
│   │       ├── api/                   # Public API interfaces
│   │       ├── domain/               # Entities (SysUser, SysRole, etc.)
│   │       ├── service/              # Business logic
│   │       ├── infrastructure/        # Repository implementations
│   │       ├── context/              # Application context
│   │       ├── security/             # Security config
│   │       ├── cache/                # Cache configuration
│   │       └── listener/            # Domain event listeners
│   │
│   ├── bms-module-quartz/            # [QUARTZ MODULE]
│   │   └── src/main/java/com/pd/modules/quartz/
│   │       ├── domain/               # Job entities
│   │       ├── service/              # Job scheduling services
│   │       ├── infrastructure/        # Repository implementations
│   │       └── listener/            # Job event listeners
│   │
│   └── bms-module-generator/          # [GENERATOR MODULE]
│       └── src/main/java/com/pd/modules/generator/
│           ├── domain/               # Template entities
│           ├── service/              # Code generation services
│           └── infrastructure/        # Repository implementations
│
├── vantage-admin/                    # APPLICATION BOOTSTRAP & GATEWAY (Port 8080)
│   └── src/main/java/com/pd/
│       ├── VantageAdminApplication.java  # Main entry point with @Modulith
│       ├── gateway/                      # [GATEWAY] REST Controllers
│       │   ├── system/                   # System REST controllers
│       │   ├── quartz/                   # Quartz REST controllers
│       │   ├── generator/                # Generator REST controller
│       │   ├── datasource/              # Datasource REST controller
│       │   ├── report/                  # Report REST controllers
│       │   └── job/                     # Job REST controller
│       ├── framework/                   # [FRAMEWORK] Core infrastructure
│       │   ├── ai/                        # AI integration (LangChain4j)
│       │   ├── config/                    # Framework configuration
│       │   └── security/                 # Security configuration
│       └── config/                      # Application configuration
│
└── vantage-ui/                       # React 19 + Vite Frontend (Root Level)
    ├── src/
    ├── public/
    └── package.json
```

### Module Dependencies

```
┌─────────────────────────────────────────────────────────┐
│            vantage-admin (Port 8080)                    │
│  ┌─────────────────────────────────────────────────┐   │
│  │  gateway/* (REST Controllers - Public API)      │   │
│  │  - Exposes vantage-modules over HTTP            │   │
│  │  - Single entry point for all REST APIs          │   │
│  └──────────────────┬──────────────────────────┘   │
│                     │ Uses (calls services)          │
│  ┌──────────────────▼──────────────────────────┐   │
│  │              vantage-modules                  │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐      │   │
│  │  │ system  │ │ quartz  │ │generator│      │   │
│  │  └─────────┘ └─────────┘ └─────────┘      │   │
│  └─────────┼─────────┼─────────┼────────┘   │
│            │         │         │                 │
│  ┌─────────┴─────────┴─────────┴────────┐   │
│  │          vantage-common                    │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

### Access Rules
1. **gateway** → can access → **vantage-modules** (via API interfaces)
2. **vantage-modules** → can access → **vantage-common**
3. **framework** (in vantage-admin) → can access → **vantage-common**
4. Cross-module access only through **public API interfaces** in `api/` packages

---

## 📦 Module Ecosystem

### 🛠 System Control Center
The heart of the platform's configuration.
- **User & Role Mgmt**: Granular RBAC permissions.
- **Dynamic Menus**: Fully customizable navigation tree.
- **Dictionary Management**: Centralized constants and lookup data.
- **System Config**: Real-time updates to SMTP and notification settings.

### ⏱ Advanced Job Scheduling
Enterprise automation built on Quartz.
- **Execution Controls**: Configurable retries, timeouts, and dependencies.
- **Notifications**: Automatic Email/Webhook alerts on failure.
- **Visual Tools**: Built-in Cron expression builder and scheduling calendar.
- **Live Observability**: Real-time execution logs via WebSockets.
- **Holiday Awareness**: Intelligent skipping based on Holiday Calendar.

### 📊 Advanced Reporting Module
- **Visual Designer**: Drag-and-drop report constructor.
- **Execution Modes**: Pure SQL or Builder mode.
- **Multi-Output**: Excel, PDF, or CSV.
- **Scheduled Delivery**: Automatic report execution with email integration.

### 📈 Real-time Monitoring Dashboard
- **System Overview**: CPU, memory, uptime, thread counts, and DB connection pool status.
- **HTTP Traffic**: Live request log with method, status, duration, and module mapping.
- **Thread Analysis**: Thread state breakdown and top busy threads.
- **Health Checks**: Ping, DB, disk space, and custom component health indicators.
- **Actuator Integration**: Polls `/actuator/info`, `/metrics`, `/threaddump`, `/health`, and `/httpexchanges` on 5–10s intervals.

---

## 🛠 Technical Architecture

### Backend Stack
- **Spring Boot 4.0.5**: Core framework with `@Modulith` support
- **Spring Modulith 1.3.2**: Module encapsulation and verification
- **Spring Security**: RBAC and method-level security
- **Spring Data JPA**: Database-agnostic persistence
- **Quartz Scheduler**: Enterprise scheduling
- **WebSocket (STOMP)**: Real-time event propagation
- **LangChain4j**: AI/LLM integration (Ollama + Qwen2.5-Coder)
- **Lombok**: Reduced boilerplate code

### Frontend Stack
- **React 19**: Modern component architecture
- **Vite 5**: Ultra-fast build tool with API/WebSocket/Actuator proxies
- **Glassmorphism Theme**: Custom CSS design system
- **Lucide Icons**: High-fidelity iconography

### Development Notes
- **JDK 17 Required** — code is compatible with Java 17+ (no Java 21+ features like switch pattern matching or `List.getFirst()`)
- **Vite `globalThis` polyfill** — `sockjs-client` requires `define: { global: 'globalThis' }` in `vite.config.js`
- **Actuator endpoints** — `/actuator/*` requests are excluded from HTTP traffic logs to avoid noise

---

## 📡 API

### Real-time WebSocket Events (`/ws-job`)
- `JOB_STARTED` / `JOB_COMPLETED` / `JOB_FAILED`
- `REPORT_GENERATED`
- `SYSTEM_CONFIG_UPDATED`

### Key REST Endpoints
| Feature | Endpoint | Description |
| :--- | :--- | :--- |
| **Auth** | `POST /api/login` | Session-based authentication |
| **Jobs** | `GET /api/system/job` | Manage scheduled automation |
| **Reports** | `POST /api/system/report/execute` | Immediate report generation |
| **Config** | `PUT /api/system/configs` | Batch update system settings |
| **Logs** | `GET /api/system/job-logs` | Live log stream access |
| **Job Health** | `GET /api/system/job-dashboard/health` | Job health monitoring |
| **Job Trends** | `GET /api/system/job-dashboard/trend` | Execution analytics |
| **Email Templates** | `GET /api/system/email-template/list` | Email templates |
| **Webhook Trigger** | `POST /api/public/job/webhook/{jobId}?token=xxx` | External job trigger |

---

## ✨ New Features (2026-04)

### Job Health Monitoring
- Stuck job detection (>1 hour running)
- Frequent failure alerts (3+ failures/week)
- `/api/system/job-dashboard/health` endpoint
- Dashboard widget showing job health status

### Report Parameters
- Parameter input dialog for parameterized reports
- JSON parameter support in report execution
- Save parameters with report template
- **Dynamic System Variables** - Auto-replaced at execution time:
  - `${SYSDATE}` - Current date (yyyy-MM-dd)
  - `${SYSDATETIME}` - Current datetime
  - `${YEAR}` / `${MONTH}` / `${DAY}` - Date parts
  - `${PREV_DAY}` / `${NEXT_DAY}` - Relative dates
  - Format: `${SYSDATE:dd/MM/yyyy}` for custom formats

Example SQL:
```sql
SELECT * FROM sales WHERE sale_date = '${SYSDATE}'
SELECT * FROM users WHERE created >= '${PREV_DAY}'
```

### Email Templates with Data Tables
- Dynamic SQL queries embedded in emails
- `${dataTable}` placeholder for rendered results
- Multiple data sources per template

### Dynamic SMTP Configuration
- SMTP settings stored in database (`sys_config`)
- Runtime reload without restart
- Supports Gmail, Office 365, custom SMTP

### PWA Support
- Offline-capable web app
- Installable on desktop/mobile
- Service worker caching

### External Job Trigger
- Secure webhook API: `/api/public/job/webhook/{jobId}?token=xxx`
- Per-job webhook tokens

### Job Dependencies
- Chain jobs with `dependent_job_ids` field
- Auto-trigger dependent jobs on success

### Real-time Monitoring Dashboard
- Custom UI replacing Spring Boot Admin (incompatible with Spring Boot 4.x)
- Overview, HTTP Traffic, Threads, and Health tabs
- Actuator endpoint polling (5s/10s intervals)
- `/actuator` calls filtered from HTTP traffic logs
- Compact inline stats on main Dashboard with clickable System Health card

### TabBar Navigation
- Horizontal scroll with `ChevronLeft`/`ChevronRight` buttons (native browser scrolling)
- Refresh button to reload active tab content
- Tab close support with selective closability

### UI Improvements
- Compact inline stats on main Dashboard (replaced 4 large stat cards)
- Sidebar "Groups" renamed to "Roles"
- Quick action "Monitor Hub" renamed to "Scheduled Jobs"
- Sidebar active state correctly highlights Monitoring tab
- `React.StrictMode` disabled in dev to prevent duplicate polling intervals

### Backend Fixes
- `OperationLogAspect.java`: Replaced Java 21+ switch pattern matching with `instanceof` for JDK 17 compatibility
- `QuartzTaskExecutor.java`: Replaced `List.getFirst()` with `list.get(0)` and switch expressions with `if-else` for JDK 17 compatibility

---

## 🚀 Future Roadmap
- [x] **Mobile Dashboard**: PWA support added
- [x] **Real-time Monitoring**: Custom actuator-based monitoring dashboard
- [ ] **AI-Powered Insights**: Anomaly detection in job execution patterns
- [ ] **Gantt Timeline**: Visualizer for complex job dependency chains
- [ ] **PDF Previewer**: Integrated browser-based PDF report viewer
- [ ] **Metric Charting**: Historical traffic/health trends with Recharts

---

## Key Features
- ✅ **Single port (8080)** - no separate gateway service
- ✅ **Modular monolith** - logical modules with clear boundaries
- ✅ **Spring Modulith compliant** - uses `@Modulith` and module detection
- ✅ **DDD architecture** - domain, service, infrastructure layers
- ✅ **API-first design** - public interfaces in `api/` packages
- ✅ **Event-driven** - domain events between modules

---

**Built with ❤️ by the Vantage Development Team**
