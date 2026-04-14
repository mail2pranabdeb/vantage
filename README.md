# Vantage Admin Platform

A modern, modular business management system built with Spring Boot and React, featuring advanced job scheduling, email notifications, report generation, and real-time monitoring.

## 🚀 Latest Release — v2.0

### Report Email Integration
- ✅ **Mandatory Email Templates for Report Jobs** — Every report job requires an email template
- ✅ **Auto-Select Default Template** — First template or marked default is pre-selected
- ✅ **Template-Based Email Rendering** — Uses `${appName}`, `${jobId}`, `${jobName}`, `${reportName}`, `${totalRows}`, etc.
- ✅ **CSV Report Attachment** — Report data attached as downloadable CSV
- ✅ **Email Group Dictionary Support** — Resolve recipients from dictionary entries
- ✅ **Template Validation** — Blocks report job creation without a template

### Dictionary Data Management
- ✅ **Full CRUD in DictDataView** — Add, Edit, Delete dictionary data entries
- ✅ **Disabled Dict Name Display** — Shows dictionary type name in disabled field
- ✅ **Backend Endpoints** — POST/PUT/DELETE for dict data, plus type lookup

### Job Scheduling Stability
- ✅ **Auto-Create Missing Jobs** — Jobs auto-registered in scheduler before manual execution
- ✅ **Orphaned Trigger Cleanup** — Stale Quartz triggers removed on startup
- ✅ **Job Edit Form Fix** — All fields (Job Type, Report, Email Group) now populate correctly
- ✅ **Status Label** — Changed "Running" → "Active" for clarity

### System Enhancements
- ✅ **Job Status: Active/Paused** — Clearer terminology across UI
- ✅ **Missing Entity Fields** — `SysDictData` audit fields added (createBy, createTime, etc.)
- ✅ **Quartz ClassCastException Fix** — Safe `JOB_LOG_ID` extraction from JobDataMap

---

## 📋 Feature Overview

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

### Report & Email Integration
- ✅ **Report Job Execution** - Execute reports on schedule via Quartz
- ✅ **Mandatory Email Template** - Required field for all report jobs
- ✅ **Template Variable Processing** - Dynamic email content from job data
- ✅ **CSV Attachment** - Report data exported and attached to emails
- ✅ **Email Group Dictionary** - Pre-configured recipient groups from system dictionary
- ✅ **Active Report Selection** - Only activated reports available in job form

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
- ✅ **Dictionary Data CRUD** - Full add/edit/delete for dictionary data entries
- ✅ **All settings persist** in database across restarts

---

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

---

## Project Structure

```
vantage-master/
├── vantage-admin/              # Main application module
│   ├── vantage-ui/             # React frontend
│   │   └── src/
│   │       ├── components/     # Reusable components
│   │       │   ├── CronBuilder.jsx          [Visual cron builder]
│   │       │   ├── DataGrid.jsx             [Data tables]
│   │       │   ├── Modal.jsx                [Dialog component]
│   │       │   └── Toast.jsx                [Notification system]
│   │       └── pages/          # Page components
│   │           ├── JobList.jsx              [ENHANCED] Job management
│   │           ├── DictList.jsx             Dictionary type management
│   │           ├── DictDataView.jsx         [NEW] Dictionary data CRUD
│   │           ├── SystemSettings.jsx       System config with tabs
│   │           ├── EmailConfig.jsx          SMTP configuration
│   │           ├── EmailTemplateManager.jsx Email template CRUD
│   │           ├── LiveJobLogs.jsx          Real-time log viewer
│   │           ├── JobCalendar.jsx          Visual job schedule
│   │           └── HolidayCalendar.jsx      Holiday management
│   └── src/main/java/          # Backend entry point
├── vantage-common/             # Shared utilities & common code
├── vantage-framework/          # Core framework components
│   └── src/main/java/com/pd/framework/config/
│       ├── WebSocketConfig.java           WebSocket configuration
│       ├── NotificationConfig.java        Email/WebSocket config
│       └── ...
├── vantage-modules/            # Business modules
│   ├── bms-module-system/      # System management
│   │   └── src/main/java/com/pd/modules/system/
│   │       ├── web/
│   │       │   ├── SysDictController.java    [ENHANCED] + dict data CRUD
│   │       │   └── SysConfigController.java  Batch save + test email
│   │       └── domain/
│   │           └── SysDictData.java          [ENHANCED] + audit fields
│   ├── bms-module-quartz/      # Job scheduling module [ENHANCED]
│   │   └── src/main/java/com/pd/modules/quartz/
│   │       ├── domain/
│   │       │   ├── SysJob.java              [ENHANCED] + email template fields
│   │       │   └── EmailTemplate.java       Email template entity
│   │       ├── service/
│   │       │   ├── EmailTemplateService.java     Template management
│   │       │   ├── JobNotificationService.java   Email/Webhook notifications
│   │       │   └── SysJobServiceImpl.java        [ENHANCED] job lifecycle
│   │       ├── util/
│   │       │   ├── QuartzTaskExecutor.java       [ENHANCED] report email
│   │       │   └── ScheduleUtils.java            [ENHANCED] safe scheduling
│   │       └── config/
│   │           └── JobInitializer.java           [ENHANCED] orphan cleanup
│   └── bms-module-generator/   # Code generation module
├── data/                       # H2 database files
├── logs/                       # Application logs
└── docs/                       # Documentation
```

---

## Quick Start

### Prerequisites
- Java 17 or higher
- Node.js 20+ (for frontend builds)
- Maven 3.6+ (or use the included Maven wrapper)
- SMTP server (for email notifications, optional)

### Build

```bash
# Linux/macOS
./mvnw clean package -pl vantage-admin -am -DskipTests

# Windows
mvnw.cmd clean package -pl vantage-admin -am -DskipTests
```

This builds all backend modules and the React frontend, packaging everything into an executable JAR.

### Run

```bash
# Run with Spring Boot
./mvnw spring-boot:run -pl vantage-admin

# Or run the packaged JAR directly
java -jar vantage-admin/target/vantage-admin-0.0.1-SNAPSHOT.jar
```

### Access the Application

| Service | URL |
|---------|-----|
| Application | http://localhost:8081 |
| H2 Console | http://localhost:8081/h2-console |
| Actuator Health | http://localhost:8081/actuator/health |

**H2 Console Login:**
- JDBC URL: `jdbc:h2:file:./data/vantage`
- Username: `sa`
- Password: `vantage123`

**Default Login Credentials:**
- Username: `admin`
- Password: `123456`

---

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
4. Copy the 16-character password

---

## Database

The application uses H2 database in file mode with Oracle compatibility. Data persists in `./data/vantage.mv.db`.

### Key Database Tables

| Table | Description |
|-------|-------------|
| `sys_user` | User accounts |
| `sys_role` | Roles |
| `sys_menu` | Menus & permissions |
| `sys_dict_type` | Dictionary types |
| `sys_dict_data` | Dictionary data entries |
| `sys_config` | System configuration |
| `sys_job` | Quartz scheduled jobs |
| `sys_job_log` | Job execution logs |
| `sys_job_email_template` | Email templates for notifications |
| `sys_holiday` | Holiday calendar |
| `gen_table` | Code generation metadata |
| `QRTZ_*` | Quartz scheduler tables (10 tables) |

---

## Modules

| Module | Description |
|--------|-------------|
| `vantage-common` | Shared utilities, base controllers, domain events, annotations, exception handling |
| `vantage-framework` | Core framework: security, caching, WebSocket, notification configuration |
| `vantage-module-system` | User/role/menu/dict/report management, login records, operation logs |
| `vantage-module-quartz` | Enhanced scheduled job management with email notifications and report integration |
| `vantage-module-generator` | Code generation for entities, repositories, services, and controllers |

---

## Features

### Authentication & Authorization
- ✅ Login/Logout with session-based authentication
- ✅ Role-based access control (RBAC)
- ✅ Permission-based method security
- ✅ Login success/failure recording
- ✅ BCrypt password hashing

### Operation Logging
- ✅ Automatic operation logging via AOP (`@Log` annotation)
- ✅ Records all REST API calls with user, IP, browser, OS, execution time

### Job Scheduling
- ✅ Quartz-based job scheduling with cron expressions
- ✅ Visual cron builder with presets
- ✅ Job execution logging with duration tracking
- ✅ Retry mechanism (configurable count & interval)
- ✅ Job timeout protection
- ✅ Email notifications with template support
- ✅ Webhook notifications
- ✅ Job dependencies (chain jobs)
- ✅ Job templates (6 pre-built)
- ✅ Bulk operations (pause/resume/delete/run)
- ✅ Import/Export (JSON)
- ✅ Holiday calendar support
- ✅ Time zone support
- ✅ Real-time status via WebSocket

### Report & Email Integration
- ✅ Report job execution on schedule
- ✅ Mandatory email template for report jobs
- ✅ Template variable rendering (`${jobName}`, `${reportName}`, `${totalRows}`, etc.)
- ✅ CSV report attachment in emails
- ✅ Email group dictionary support

### Monitoring & Dashboard
- ✅ Dashboard metrics (success rate, avg duration, job counts)
- ✅ Live job logs (real-time streaming viewer)
- ✅ Job calendar (visual schedule view)
- ✅ Holiday calendar management
- ✅ 30-day performance trends
- ✅ WebSocket real-time updates

### Code Generation
- ✅ Entity, repository, service, and controller generation
- ✅ Supports CRUD and tree templates

### Observability
- ✅ Spring Actuator endpoints (health, info, metrics)
- ✅ Prometheus metrics export
- ✅ Distributed tracing (Micrometer Tracing)
- ✅ Structured logging with trace/span IDs

---

## Suggested Future Features

Here are features worth considering for the next release:

### 1. **PDF/Excel Report Attachments**
- Convert CSV to formatted PDF or Excel files for email attachments
- Use Apache POI for Excel or iText for PDF generation

### 2. **Report Scheduler UI Builder**
- Drag-and-drop cron expression builder for report jobs
- Visual schedule preview with next 10 execution dates

### 3. **Email Template Preview**
- Live preview of rendered email template with sample data before saving
- Test send button on the template management page

### 4. **Multi-Database Support**
- Switch between H2, MySQL, PostgreSQL via configuration
- Migration scripts for schema across databases

### 5. **Audit Trail**
- Track all CRUD operations on sensitive entities (users, roles, jobs, reports)
- Display audit history with before/after values

### 6. **User Activity Dashboard**
- Show recent logins, active sessions, failed login attempts
- Session management (force logout, lock users)

### 7. **Email Delivery Tracking**
- Track sent/failed/bounced emails per job
- Retry failed email deliveries independently

### 8. **Role-Based Job Access**
- Restrict who can create/edit/run/delete specific jobs
- Job-level permissions tied to roles

### 9. **Dashboard Widget Builder**
- Drag-and-drop dashboard widget creation
- Custom SQL queries for widget data sources

### 10. **API Key Management**
- Generate/manage API keys for external integrations
- Rate limiting per API key

### 11. **Scheduled Report Subscription**
- Allow users to subscribe/unsubscribe from report emails
- Personal subscription management page

### 12. **Notification Center**
- In-app notification bell for job failures, system alerts
- Mark as read/unread, notification history

### 13. **Data Source Connector UI**
- Visual database connection tester and browser
- Run ad-hoc queries from the UI

### 14. **Backup & Restore**
- One-click database backup/restore
- Export/import all configuration as ZIP

### 15. **Mobile Responsive UI**
- Full responsive design for mobile/tablet access
- Touch-friendly controls

---

## API Endpoints

### Authentication
- `POST /api/login` - User login
- `POST /api/logout` - User logout

### System
- `GET/POST/PUT/DELETE /api/system/users` - User management
- `GET/POST/PUT/DELETE /api/system/roles` - Role management
- `GET/POST/PUT/DELETE /api/system/menus` - Menu management
- `GET/POST/PUT/DELETE /api/system/dict/type/*` - Dictionary type management
- `GET/POST/PUT/DELETE /api/system/dict/data/*` - Dictionary data management [NEW]
- `GET/POST/PUT/DELETE /api/system/configs` - Config management (batch)
- `GET/POST/PUT/DELETE /api/system/notices` - Notice management
- `POST /api/system/config/test-email` - Test email connection
- `GET /api/system/dict/type/get-by-code/{dictType}` - Lookup dict type by code [NEW]

### Jobs
- `GET/POST/PUT/DELETE /api/system/job` - Job management
- `POST /api/system/job/run` - Execute job immediately
- `PUT /api/system/job/changeStatus` - Change job status
- `GET/POST/PUT/DELETE /api/system/email-template` - Email template management
- `GET /api/system/job-dashboard/metrics` - Dashboard metrics
- `GET /api/system/job-log/job/{jobId}` - Job execution logs

### Generator
- `GET /generator/list` - List tables for generation
- `POST /generator/code` - Generate code

---

## Troubleshooting

### Email Not Sending
1. Check SMTP settings in System Settings → Email tab
2. Use Gmail App Password, not regular password
3. Enable TLS for port 587
4. Click "Test Connection" to verify

### Jobs Not Executing
1. Ensure job status is "Active" (not "Paused")
2. Check cron expression validity
3. Verify invoke target method exists
4. Check Live Logs for execution errors

### Report Emails Not Received
1. Ensure an Email Template is selected on the job form
2. Verify email group or notification emails are configured
3. Check application logs for "No email template configured" warning

### Login Issues
- Default credentials: `admin` / `123456`
- Password is BCrypt hashed (use `HashGen.java` to generate new hashes)

---

## License

MIT License

---

**Built with ❤️ using Spring Boot + React**
