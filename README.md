# Vantage Admin Platform

A modern, modular business management system built with Spring Boot and React, featuring advanced job scheduling, email notifications, and real-time monitoring.

## 🚀 New Features (Latest Update)

### Job Module Enhancement
- ✅ **Job Execution History** - Database logging for all job executions
- ✅ **Retry Mechanism** - Configurable auto-retry for failed jobs
- ✅ **Job Timeout** - Maximum execution time protection
- ✅ **Email Notifications** - Send alerts on job failures via SMTP
- ✅ **Webhook Support** - External system notifications
- ✅ **Job Dependencies** - Chain jobs together (trigger downstream jobs)
- ✅ **Job Templates** - 6 pre-built templates for common tasks
- ✅ **Bulk Operations** - Pause/resume/delete/run multiple jobs
- ✅ **Import/Export** - JSON-based job configuration backup
- ✅ **Cron Builder** - Visual cron expression builder with presets
- ✅ **Holiday Calendar** - Skip job execution on holidays
- ✅ **Time Zone Support** - Schedule jobs in different time zones

### Monitoring & Observability
- ✅ **Dashboard Metrics** - Success rate, avg execution time, job counts
- ✅ **Real-time Status** - WebSocket-based live updates
- ✅ **Live Job Logs** - Real-time log streaming UI
- ✅ **Performance Analytics** - 30-day trend analysis
- ✅ **Email Template Management** - HTML templates with variables

### System Configuration
- ✅ **System Settings** - Unified configuration with tabs
  - Basic Settings (name, logo, favicon, copyright)
  - Email Configuration (SMTP with test connection)
  - Notification Preferences
- ✅ **All settings persist** in database across restarts

## Tech Stack

### Backend
- **Java 17** + **Spring Boot 4.0.3**
- **Spring Security** - Authentication & Authorization
- **Spring MVC** - Web Layer
- **Spring Data JPA** - Persistence Framework
- **H2 Database** - Embedded Database (file-based at `./data/vantage`)
- **Quartz** - Job Scheduling with enhancements
- **Spring WebSocket** - Real-time updates
- **JavaMail** - Email notifications
- **Apache Velocity** - Template Engine (code generation)
- **Caffeine** - Local Caching
- **Micrometer** - Metrics & Observability (Prometheus, Tracing)

### Frontend
- **React 19** + **Vite**
- **React Router** - Client-side Routing
- **Lucide React** - Icon Library
- **STOMP.js** - WebSocket client for real-time updates

## Project Structure

```
vantage-master/
├── vantage-admin/              # Main application module
│   ├── vantage-ui/             # React frontend
│   │   └── src/
│   │       ├── components/     # Reusable components
│   │       │   ├── CronBuilder.jsx          [NEW] Visual cron builder
│   │       │   └── ...
│   │       └── pages/          # Page components
│   │           ├── JobList.jsx              [ENHANCED] Job management
│   │           ├── SystemSettings.jsx       [NEW] System config with tabs
│   │           ├── EmailConfig.jsx          [NEW] SMTP configuration
│   │           ├── EmailTemplateManager.jsx [NEW] Email template CRUD
│   │           ├── LiveJobLogs.jsx          [NEW] Real-time log viewer
│   │           ├── JobCalendar.jsx          [NEW] Visual job schedule
│   │           └── HolidayCalendar.jsx      [NEW] Holiday management
│   └── src/main/java/          # Backend entry point
├── vantage-common/             # Shared utilities & common code
├── vantage-framework/          # Core framework components
│   └── src/main/java/com/pd/framework/config/
│       ├── WebSocketConfig.java           [NEW] WebSocket configuration
│       ├── NotificationConfig.java        [NEW] Email/WebSocket config
│       └── ...
├── vantage-modules/            # Business modules
│   ├── bms-module-system/      # System management
│   │   └── src/main/java/com/pd/modules/system/
│   │       ├── controller/
│   │       │   ├── SysConfigController.java    [ENHANCED] Batch save + test email
│   │       │   └── EmailConfigController.java  [NEW] Email config API
│   │       └── config/
│   │           └── PostStartupSqlRunner.java   [NEW] SQL initialization
│   ├── bms-module-quartz/      # Job scheduling module [ENHANCED]
│   │   └── src/main/java/com/pd/modules/quartz/
│   │       ├── domain/
│   │       │   ├── SysJob.java              [ENHANCED] +11 new fields
│   │       │   ├── SysJobLog.java           [ENHANCED] Execution logging
│   │       │   ├── EmailTemplate.java       [NEW] Email template entity
│   │       │   └── SysHoliday.java          [NEW] Holiday calendar entity
│   │       ├── service/
│   │       │   ├── EmailTemplateService.java     [NEW] Template management
│   │       │   ├── JobNotificationService.java   [NEW] Email/Webhook notifications
│   │       │   ├── JobDependencyService.java     [NEW] Job chaining
│   │       │   ├── JobMetricsService.java        [NEW] Dashboard metrics
│   │       │   ├── JobTemplateService.java       [NEW] Job templates
│   │       │   └── JobWebSocketService.java      [NEW] Real-time updates
│   │       ├── controller/
│   │       │   ├── EmailTemplateController.java  [NEW] Template CRUD API
│   │       │   ├── JobLogController.java         [NEW] Execution logs API
│   │       │   ├── JobDashboardController.java   [NEW] Metrics API
│   │       │   └── JobTemplateController.java    [NEW] Template API
│   │       └── config/
│   │           └── JobInitializer.java           [NEW] Load jobs on startup
│   └── bms-module-generator/   # Code generation module
├── data/                       # H2 database files
├── logs/                       # Application logs
└── docs/                       # Documentation
    ├── JOB_ENHANCEMENTS.md     [NEW] Complete feature docs
    ├── SETUP_GUIDE.md          [NEW] Setup instructions
    └── ROUTES_TO_ADD.md        [NEW] React router config
```

## Quick Start

### Prerequisites
- Java 17 or higher
- Node.js 20+ (for frontend builds)
- Maven 3.6+ (or use the included Maven wrapper)
- Gmail account (for email notifications, optional)

### Build (Backend + Frontend)

Build everything in a single command:

```bash
./mvnw clean package -pl vantage-admin -am -DskipTests
```

On Windows:
```cmd
mvnw.cmd clean package -pl vantage-admin -am -DskipTests
```

This builds all backend modules and the React frontend, packaging everything into an executable JAR.

**Note:** The build process automatically installs Node.js and npm, then builds the frontend. This happens during the `generate-resources` phase via the `frontend-maven-plugin`.

### Run

```bash
# Run with Spring Boot
./mvnw spring-boot:run -pl vantage-admin

# Or run the packaged JAR directly
java -jar vantage-admin/target/vantage-admin-0.0.1-SNAPSHOT.jar
```

On Windows:
```cmd
mvnw.cmd spring-boot:run -pl vantage-admin
:: Or
java -jar vantage-admin\target\vantage-admin-0.0.1-SNAPSHOT.jar
```

### Access the Application

| Service | URL |
|---------|-----|
| Application | http://localhost:8081 |
| H2 Console | http://localhost:8081/h2-console |
| Actuator Health | http://localhost:8081/actuator/health |
| Actuator Metrics | http://localhost:8081/actuator/metrics |
| Actuator Prometheus | http://localhost:8081/actuator/prometheus |

**H2 Console Login:**
- JDBC URL: `jdbc:h2:file:./data/vantage`
- Username: `sa`
- Password: *(leave blank)*

**Default Login Credentials:**
- Username: `admin`
- Password: `123456`

## Development

### Backend

```bash
# Compile all modules
./mvnw clean install

# Run with hot reload
./mvnw spring-boot:devtools -pl vantage-admin
```

### Frontend

```bash
cd vantage-admin/vantage-ui

# Install dependencies
npm install

# Start dev server
npm run dev

# Build for production
npm run build
```

## Configuration

### Email Configuration (SMTP)

Configure email notifications via the UI:
1. Go to **System Management** → **Config Management**
2. Click **Email** tab
3. Enter SMTP settings:
   ```
   SMTP Host: smtp.gmail.com
   SMTP Port: 587
   Username: your-email@gmail.com
   Password: [Gmail App Password - 16 chars]
   Enable Authentication: ✓
   Enable TLS: ✓
   From Email: your-email@gmail.com
   From Name: Vantage Admin
   ```
4. Click **Test Connection** to verify
5. Click **Save Email Settings**

**Getting Gmail App Password:**
1. Enable 2-Factor Authentication on your Google account
2. Go to: https://myaccount.google.com/apppasswords
3. Select App: **Mail**, Device: **Other** → Enter "Vantage"
4. Click **Generate**
5. Copy the 16-character password (remove spaces)

### Application Configuration

Edit [`vantage-admin/src/main/resources/application.yml`](vantage-admin/src/main/resources/application.yml) to customize:

- Server port (default: 8081)
- Database connection (H2 file-based with Oracle compatibility)
- Logging levels
- Actuator endpoints
- Tracing sampling
- AI chat assistant settings

## Database

The application uses H2 database in file mode with Oracle compatibility. Data persists in `./data/vantage.mv.db`.

Schema and initial data are loaded from:
- `classpath:schema.sql` - Database schema (21+ tables)
- `classpath:data.sql` - Initial seed data
- `classpath:data-pending-features.sql` - Additional tables for new features

### Database Tables

| Table | Description |
|-------|-------------|
| `sys_user` | User accounts |
| `sys_role` | Roles |
| `sys_menu` | Menus & permissions |
| `sys_user_role` | User-Role mapping |
| `sys_role_menu` | Role-Menu mapping |
| `sys_user_post` | User-Post mapping |
| `sys_post` | Job positions |
| `sys_dict_type` | Dictionary types |
| `sys_dict_data` | Dictionary data |
| `sys_config` | System configuration |
| `sys_logininfor` | Login audit logs |
| `sys_operlog` | Operation audit logs |
| `sys_notice` | System notices |
| `sys_job` | Quartz scheduled jobs (enhanced) |
| `sys_job_log` | Job execution logs (enhanced) |
| `sys_job_email_template` | Email templates for notifications [NEW] |
| `sys_holiday` | Holiday calendar [NEW] |
| `sys_job_version` | Job configuration versioning [NEW] |
| `sys_job_role` | Job-role access control [NEW] |
| `sys_job_execution` | Job execution history for Gantt [NEW] |
| `gen_table` | Code generation table metadata |
| `gen_table_column` | Code generation column metadata |
| `QRTZ_*` | Quartz scheduler tables (10 tables) |

## Modules

| Module | Description |
|--------|-------------|
| `vantage-common` | Shared utilities, base controllers, domain events, annotations, exception handling |
| `vantage-framework` | Core framework: security config, cache config, WebSocket config, notification config |
| `vantage-module-system` | User/role/menu management, login records, operation logs, config, dict, posts, notices, email config |
| `vantage-module-quartz` | Enhanced scheduled job management with email notifications, templates, dependencies |
| `vantage-module-generator` | Code generation for entities, repositories, services, and controllers |

## Features

### Authentication & Authorization
- ✅ Login/Logout with session-based authentication
- ✅ Role-based access control (RBAC)
- ✅ Permission-based method security
- ✅ Login success/failure recording (`sys_logininfor`)
- ✅ BCrypt password hashing (use `HashGen.java` to generate hashes)

### Operation Logging
- ✅ Automatic operation logging via AOP (`@Log` annotation)
- ✅ Records all REST API calls
- ✅ Tracks: user, IP, browser, OS, execution time, request/response
- ✅ Query and filter operations (`sys_oper_log`)

### System Management
- ✅ User management (CRUD)
- ✅ Role management
- ✅ Menu management
- ✅ **System Settings** (enhanced with tabs) [NEW]
- ✅ Dict management
- ✅ Post management
- ✅ Notice management

### Job Scheduling [ENHANCED]
- ✅ Quartz-based job scheduling
- ✅ Cron expression support
- ✅ **Visual cron builder** with presets [NEW]
- ✅ Job execution logging with duration [NEW]
- ✅ **Retry mechanism** (configurable count & interval) [NEW]
- ✅ **Job timeout** protection [NEW]
- ✅ **Email notifications** on failure [NEW]
- ✅ **Webhook notifications** [NEW]
- ✅ **Job dependencies** (chain jobs) [NEW]
- ✅ **Job templates** (6 pre-built) [NEW]
- ✅ **Bulk operations** (pause/resume/delete/run) [NEW]
- ✅ **Import/Export** (JSON) [NEW]
- ✅ **Holiday calendar** support [NEW]
- ✅ **Time zone** support [NEW]
- ✅ **Real-time status** via WebSocket [NEW]

### Email Integration [NEW]
- ✅ **Email Template Management** - HTML templates with variables
- ✅ **SMTP Configuration** - Via UI with test connection
- ✅ **Job Failure Notifications** - Automatic emails on job failure
- ✅ **Custom Templates** - Create/edit/delete templates
- ✅ **Template Variables** - ${jobName}, ${executionTime}, ${errorMessage}, etc.

### Monitoring & Dashboard [NEW]
- ✅ **Dashboard Metrics** - Success rate, avg duration, job counts
- ✅ **Live Job Logs** - Real-time streaming viewer
- ✅ **Job Calendar** - Visual schedule view
- ✅ **Holiday Calendar** - Manage holidays
- ✅ **Performance Analytics** - 30-day trends
- ✅ **WebSocket Updates** - Real-time status changes

### Code Generation
- ✅ Entity generation (JPA entities)
- ✅ Repository generation
- ✅ Service layer generation
- ✅ Controller generation
- ✅ Supports CRUD and tree templates

### Observability
- ✅ Spring Actuator endpoints (health, info, metrics)
- ✅ Prometheus metrics export
- ✅ Distributed tracing (Micrometer Tracing)
- ✅ Structured logging with trace/span IDs

### Additional Features
- ✅ Real-time chat interface (WebSocket-based)
- ✅ Dashboard with metrics overview
- ✅ Responsive UI with sidebar navigation
- ✅ **Toast notifications** [NEW]

## New Pages

| Page | URL | Description |
|------|-----|-------------|
| System Settings | `/system/config` | Unified config with Basic/Email/Notifications tabs |
| Email Configuration | `/system/email-config` | SMTP settings and test connection |
| Email Templates | `/system/email-templates` | HTML template CRUD with preview |
| Live Job Logs | `/system/job-logs` | Real-time job execution viewer |
| Job Calendar | `/system/job-calendar` | Visual job schedule calendar |
| Holiday Calendar | `/system/holiday-calendar` | Manage holidays for job scheduling |

## API Endpoints

### Authentication
- `POST /api/login` - User login
- `POST /api/logout` - User logout

### System
- `GET/POST/PUT/DELETE /api/system/users` - User management
- `GET/POST/PUT/DELETE /api/system/roles` - Role management
- `GET/POST/PUT/DELETE /api/system/menus` - Menu management
- `GET/POST/PUT/DELETE /api/system/posts` - Post management
- `GET/POST/PUT/DELETE /api/system/dicts` - Dictionary management
- `GET/POST/PUT/DELETE /api/system/configs` - Config management (batch)
- `GET/POST/PUT/DELETE /api/system/notices` - Notice management
- `GET /api/system/logininfor` - Login records
- `GET /api/system/operlog` - Operation logs
- `POST /api/system/config/test-email` - Test email connection [NEW]

### Jobs [ENHANCED]
- `GET/POST/PUT/DELETE /api/system/job` - Job management
- `DELETE /api/system/job/batch` - Bulk delete [NEW]
- `PUT /api/system/job/batch/pause` - Bulk pause [NEW]
- `PUT /api/system/job/batch/resume` - Bulk resume [NEW]
- `POST /api/system/job/batch/run` - Bulk run [NEW]
- `GET /api/system/job/export` - Export jobs [NEW]
- `POST /api/system/job/import` - Import jobs [NEW]
- `GET /api/system/job/groups` - Get job groups [NEW]
- `GET /api/system/job-log/list` - Job execution logs [NEW]
- `GET /api/system/job-log/statistics` - Log statistics [NEW]
- `GET /api/system/job-dashboard/metrics` - Dashboard metrics [NEW]
- `GET /api/system/job-dashboard/trend` - Execution trends [NEW]
- `GET/POST/PUT/DELETE /api/system/email-template` - Email templates [NEW]
- `GET/POST /api/system/job-template` - Job templates [NEW]

### Generator
- `GET /generator/list` - List tables for generation
- `POST /generator/code` - Generate code

## Utility Scripts

### HashGen.java
Generate BCrypt password hashes:
```bash
# Compile
javac -cp "path/to/spring-security-crypto.jar" HashGen.java

# Run
java -cp ".;path/to/spring-security-crypto.jar" HashGen
```

Default password: `123456`

## AI Chat Assistant

The application includes an AI-powered chat assistant with RAG (Retrieval-Augmented Generation).

### Prerequisites

1. **Install Ollama**: Download from [ollama.ai](https://ollama.ai)

2. **Pull a language model** (choose based on your RAM):
   ```bash
   # For 8-16GB RAM (BEST - Only 637MB!)
   ollama pull tinyllama       # 1.1B model, ~637MB RAM, very fast

   # For 16-32GB RAM (Good quality)
   ollama pull mistral         # 7B model, ~4-6GB RAM, good quality

   # For 32GB+ RAM (Best quality)
   ollama pull llama3          # 8B model, ~8GB RAM, very good
   ```

3. **Enable AI in configuration** (`application.yml`):
   ```yaml
   ai:
     enabled: true
     chat-model: phi3  # Match the model you pulled
   ```

4. **Restart the application**

### Model Comparison

| Model | Parameters | RAM Required | Speed | Quality | Best For |
|-------|-----------|--------------|-------|---------|----------|
| TinyLlama | 1.1B | 637MB | ⚡⚡⚡ Very Fast | Good | Laptops, 8-16GB RAM |
| Mistral | 7B | 4-6GB | ⚡⚡ Fast | Very Good | 16-32GB RAM |
| Llama 3 | 8B | 8GB | ⚡ Medium | Very Good | 32GB+ RAM |

### Features

- **Natural Language Chat**: Ask questions about system features
- **Tool Integration**: Create users, roles, manage system via chat
- **RAG Knowledge Base**: 10 pre-loaded system documentation articles
- **Conversation Memory**: Remembers context across multiple messages
- **Fallback Mode**: Works without AI (rule-based responses)

### Access

Navigate to **Chat** in the admin panel to use the AI assistant.

## Documentation

Additional documentation is available in the `vantage-modules/bms-module-quartz/` directory:

- **JOB_ENHANCEMENTS.md** - Complete feature documentation
- **SETUP_GUIDE.md** - Setup and configuration guide
- **ROUTES_TO_ADD.md** - React router configuration guide

## Troubleshooting

### Email Not Sending
1. Check SMTP settings in System Settings → Email tab
2. Use Gmail App Password, not regular password
3. Enable TLS for port 587
4. Click "Test Connection" to verify

### Jobs Not Executing
1. Ensure job status is "Running" (not "Paused")
2. Check cron expression validity
3. Verify invoke target method exists
4. Check Live Logs for execution errors

### Login Issues
- Default credentials: `admin` / `123456`
- Password is BCrypt hashed (use HashGen.java to generate new hashes)

## License

MIT License

---

**Built with ❤️ using Spring Boot + React**
