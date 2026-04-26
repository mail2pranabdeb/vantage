# Vantage Admin - Complete Documentation

## Table of Contents
1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Architecture](#architecture)
4. [Features](#features)
5. [Module Documentation](#module-documentation)
6. [API Reference](#api-reference)
7. [Database Schema](#database-schema)
8. [Troubleshooting](#troubleshooting)

---

## Overview

**Vantage Admin** is a comprehensive enterprise administration platform built with Spring Boot and React. It provides complete management capabilities for users, roles, menus, jobs, reports, and system configuration.

### Tech Stack
- **Backend:** Spring Boot 4.x, JPA, Quartz, H2 Database
- **Frontend:** React 18, Vite, Modern CSS
- **Database:** H2 (file-based), Oracle mode
- **Security:** Spring Security with JWT

### Key Features
- 👥 User & Role Management
- 🔐 Menu & Permission System
- ⚙️ Job Scheduling with Quartz
- 📊 Report Generation & Execution
- 📝 Code Generation from Tables
- 📧 Email Notifications
- 🔍 Login & Operation Logging
- 💾 Cache Management
- 📚 Dictionary Management

---

## Getting Started

### Prerequisites
- Java 17+
- Node.js 20+
- Maven 3.8+

### Build & Run

```bash
# 1. Build the application
mvn clean install -DskipTests

# 2. Start the server
cd vantage-admin
java -jar target/vantage-admin-0.0.1-SNAPSHOT.jar

# 3. Access the application
# URL: http://localhost:8081
```

### Default Credentials
```
Username: admin
Password: 123456
```

### Database Access
```
H2 Console: http://localhost:8081/h2-console
JDBC URL: jdbc:h2:file:../data/vantage
Username: sa
Password: (empty)
```

---

## Architecture

### Project Structure
```
vantage-master/
├── vantage-admin/          # Main application
│   ├── src/main/
│   │   ├── java/          # Java source
│   │   ├── resources/     # Config files
│   │   └── static/        # Built React UI
│   └── vantage-ui/        # React source
│       └── src/pages/     # Page components
├── vantage-common/        # Shared utilities
├── vantage-framework/     # Core framework
└── vantage-modules/       # Business modules
    ├── bms-module-system/    # System module
    ├── bms-module-quartz/    # Job scheduling
    └── bms-module-generator/ # Code generation
```

### Module Initialization
The application uses module-based initialization controlled by `application.yml`:

```yaml
module:
  init:
    system:
      enabled: true    # Enable system module init
    quartz:
      enabled: true    # Enable quartz module init
    generator:
      enabled: true    # Enable generator module init
  init-on-fresh-db: true  # Initialize on fresh database
```

---

## Features

### 1. Menu Management
**Location:** System Management → Menu Management

**Features:**
- Tree view with expand/collapse
- Add/Edit/Delete menus, submenus, buttons
- Three menu types:
  - **Directory (M)** - Container for submenus
  - **Menu (C)** - Clickable page link
  - **Button (F)** - Permission-only item

**Usage:**
1. Click "Add Root Menu" for top-level menu
2. Click "+" on any row to add submenu
3. Click Edit to modify menu properties
4. Set visibility and permissions

### 2. Role Management
**Location:** System Management → Role Management

**Features:**
- Create/Edit/Delete roles
- Assign menu permissions via tree view
- Data scope configuration
- Admin role gets all permissions automatically

**Usage:**
1. Click "Add Role" to create new role
2. Click Shield icon to assign permissions
3. Check/uncheck menus in permission tree
4. Click "Save Permissions"

### 3. Job Scheduling
**Location:** Job Management → Job List

**Features:**
- Cron-based scheduling
- Retry mechanism (configurable count & interval)
- Timeout protection
- Email/Webhook notifications
- Job dependencies
- Import/Export (JSON)
- Bulk operations

**Job Configuration:**
```json
{
  "jobName": "Data Sync",
  "jobGroup": "DEFAULT",
  "invokeTarget": "dataSyncService.sync()",
  "cronExpression": "0 0/30 * * * ?",
  "misfirePolicy": "3",
  "concurrent": "1",
  "maxRetryCount": 3,
  "retryInterval": 60,
  "timeoutSeconds": 3600,
  "notifyOnFailure": true,
  "notificationEmails": "admin@example.com"
}
```

### 4. Report Management
**Location:** Report Management → Report List

**Features:**
- Custom SQL report creation
- Parameter substitution (`:paramName`)
- Execute reports on-demand
- Download as Excel (CSV)
- Email with attachment
- Schedule with cron
- Execution history

**Creating a Report:**
1. Click "Add Report"
2. Enter report name and key
3. Write SQL query (use `:param` for parameters)
4. Configure output format (Excel/PDF/CSV/HTML)
5. Optional: Enable schedule and email
6. Click "Create"

**Executing a Report:**
1. Click Play button (▶️)
2. Enter parameters as JSON: `{"status":"0"}`
3. Click OK
4. Choose to download results

**Example SQL with Parameters:**
```sql
SELECT user_name, email, create_time 
FROM sys_user 
WHERE status = :status 
AND create_time >= :startDate
```

### Dynamic System Variables for Reports
**Available Variables (auto-replaced at execution time):**
- `${SYSDATE}` - Current date (format: yyyy-MM-dd)
- `${SYSDATE:dd/MM/yyyy}` - Date with custom format
- `${SYSDATETIME}` - Current datetime (format: yyyy-MM-dd HH:mm:ss)
- `${SYSDATETIME:mm/dd/yyyy HH:mm}` - Datetime with custom format
- `${YEAR}` - Current year (e.g., 2026)
- `${MONTH}` - Current month with leading zero (e.g., 04)
- `${DAY}` - Current day with leading zero (e.g., 25)
- `${PREV_DAY}` - Previous day (yyyy-MM-dd)
- `${NEXT_DAY}` - Next day (yyyy-MM-dd)

**Using Dynamic Variables in SQL:**
```sql
-- Get today's sales
SELECT * FROM sales WHERE sale_date = '${SYSDATE}'

-- Get yesterday's data
SELECT * FROM users WHERE created_date >= '${PREV_DAY}'

-- Get current month's orders
SELECT * FROM orders WHERE month = '${MONTH}' AND year = '${YEAR}'

-- Custom format example
SELECT * FROM events WHERE event_date >= '${SYSDATE:dd-MMM-yyyy}'
```

**Runtime Parameter Override:**
When executing a report job manually, you can override parameters via the popup modal. JSON format:
```json
{"status": "0", "category": "SALES"}
```

These dynamic variables are replaced automatically at job execution time - both for manual runs and scheduled runs via Quartz!

### 5. Code Generation
**Location:** Tool → Code Generation

**Features:**
- Select tables from database
- Configure generation options
- Generate Entity/Repository/Service/Controller
- Download as ZIP file
- Preview before generating

**Usage:**
1. Select tables from grid (or "Select All")
2. Click "Generate" to open configuration
3. Set package name, author, module
4. Choose components to generate
5. Click "Generate Code"
6. Download ZIP file

**Generated Structure:**
```
generated-code.zip
├── entity/User.java
├── repository/UserRepository.java
├── service/UserService.java
└── controller/UserController.java
```

### 6. Email Configuration
**Location:** System Management → Config Management → Email Config

**Configuration:**
```
SMTP Host: smtp.gmail.com
SMTP Port: 587
Username: your-email@gmail.com
Password: your-app-password
From Email: your-email@gmail.com
From Name: Vantage Admin
Enable TLS: ✓
Enable Auth: ✓
```

### 7. System Settings
**Location:** System Management → Config Management

**Tabs:**
- **Basic** - System name, logo, copyright
- **Email** - SMTP configuration
- **Notifications** - Alert preferences

---

## API Reference

### Authentication
```http
POST /api/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}

Response:
{
  "code": 200,
  "msg": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Menu APIs
```http
GET /api/system/menu/list
GET /api/system/menu/tree
POST /api/system/menu
PUT /api/system/menu
DELETE /api/system/menu/{menuId}
```

### Role APIs
```http
GET /api/system/role/list
GET /api/system/role/menuIds/{roleId}
POST /api/system/role
PUT /api/system/role
PUT /api/system/role/authDataScope
DELETE /api/system/role/{roleId}
```

### Job APIs
```http
GET /api/system/job/list
POST /api/system/job
PUT /api/system/job
DELETE /api/system/job/{jobIds}
POST /api/system/job/run
PUT /api/system/job/status
```

### Report APIs
```http
GET /api/system/report/list
POST /api/system/report
PUT /api/system/report
DELETE /api/system/report/{reportId}
POST /api/system/report/execute/{reportId}
GET /api/system/report/download/{reportId}
```

### Code Generation APIs
```http
GET /api/tool/gen/db/tables
GET /api/tool/gen/preview?table={tableName}
POST /api/tool/gen/batch
GET /api/tool/gen/download?tables={table1,table2}
```

---

## Database Schema

### Core Tables

#### sys_user (Users)
```sql
CREATE TABLE sys_user (
  user_id BIGINT PRIMARY KEY,
  login_name VARCHAR(50),
  user_name VARCHAR(50),
  password VARCHAR(100),
  status VARCHAR(1),
  -- ... more fields
);
```

#### sys_role (Roles)
```sql
CREATE TABLE sys_role (
  role_id BIGINT PRIMARY KEY,
  role_name VARCHAR(50),
  role_key VARCHAR(50),
  data_scope VARCHAR(1),
  status VARCHAR(1)
);
```

#### sys_menu (Menus)
```sql
CREATE TABLE sys_menu (
  menu_id BIGINT PRIMARY KEY,
  menu_name VARCHAR(50),
  parent_id BIGINT,
  menu_type VARCHAR(1),  -- M=Directory, C=Menu, F=Button
  perms VARCHAR(100),
  visible VARCHAR(1)
);
```

#### sys_job (Scheduled Jobs)
```sql
CREATE TABLE sys_job (
  job_id BIGINT PRIMARY KEY,
  job_name VARCHAR(64),
  cron_expression VARCHAR(255),
  invoke_target VARCHAR(500),
  status VARCHAR(1),
  max_retry_count INT,
  timeout_seconds INT
);
```

#### sys_report (Reports)
```sql
CREATE TABLE sys_report (
  report_id BIGINT PRIMARY KEY,
  report_name VARCHAR(100),
  report_key VARCHAR(50),
  sql_content CLOB,
  output_format VARCHAR(20),
  schedule_enabled BOOLEAN,
  email_enabled BOOLEAN,
  status VARCHAR(1)
);
```

---

## Troubleshooting

### Application Won't Start
**Problem:** Port 8081 already in use
```bash
# Windows: Find and kill process
netstat -ano | findstr :8081
taskkill /F /PID <PID>
```

### Login Fails
**Problem:** Admin user not found
```sql
-- Check if admin exists
SELECT * FROM sys_user WHERE login_name='admin';

-- If missing, restart with fresh database
-- Delete data/vantage.mv.db and restart
```

### Menu Not Showing
**Problem:** Menu cache not refreshed
```
Solution: Logout and login again
Or: Clear sys_config cache
```

### Jobs Not Executing
**Problem:** Quartz not initialized
```
Check logs for: "Initializing scheduled jobs"
Verify: module.init.quartz.enabled=true
```

### Report Execution Fails
**Problem:** SQL syntax error
```
Check: Parameter format (:paramName)
Verify: Table/column names exist
Test SQL in H2 Console first
```

### Code Generation Fails
**Problem:** Tables not found
```
Verify: Database connection working
Check: Table exists in PUBLIC schema
Test: GET /api/tool/gen/db/tables
```

---

## Best Practices

### Security
1. Change default admin password after first login
2. Use role-based access control
3. Enable login failure notifications
4. Regular audit of user permissions

### Performance
1. Configure connection pool size based on load
2. Use cache for frequently accessed data
3. Schedule heavy jobs during off-peak hours
4. Monitor job execution times

### Maintenance
1. Regular database backups (data/ folder)
2. Clean old job logs periodically
3. Archive executed reports
4. Review and clean inactive users

---

## Support

For issues or questions:
1. Check this documentation first
2. Review application logs (app.log)
3. Check H2 Console for database issues
4. Review startup logs for initialization errors

---

---

## New Features (2026-04)

### Job Analytics Dashboard
**Location:** Dashboard → Job Metrics

**New Metrics:**
- Most Failed Jobs (last 30 days)
- Slowest Jobs (average execution time)
- `/api/system/job-dashboard/trend` - execution trends

### Job Health Monitoring
**Endpoint:** `GET /api/system/job-dashboard/health`

**Returns:**
- `stuckJobs` - Jobs running > 1 hour
- `missedJobs` - Active jobs not run in 24 hours
- `frequentFailures` - Jobs with 3+ failures in 7 days
- `healthStatus` - "healthy" or "warning"

### Email Templates with Data Tables
**Location:** System Management → Email Templates

**Features:**
- Create HTML email templates with variables: `${jobName}`, `${message}`, `${executionTime}`
- Add Data Tables from SQL queries executed at template preview/send time
- Use `${dataTable}` placeholder in email body
- Support multiple data tables with custom SQL

**Example Data Table:**
```json
[{
  "datasourceKey": "primary",
  "query": "SELECT * FROM sys_user LIMIT 10",
  "label": "Users:",
  "enabled": true
}]
```

### Dynamic SMTP Configuration
SMTP settings are now stored in `sys_config` table and read at runtime:
- `mail.host` - SMTP server
- `mail.port` - SMTP port
- `mail.username` - SMTP username
- `mail.password` - SMTP password
- `mail.fromEmail` - From email address
- `mail.fromName` - From display name

### External Job Trigger (Webhook)
**Endpoint:** `POST /api/public/job/webhook/{jobId}?token={token}`

**Security:** Each job has a unique `webhookToken` for secure triggering.

### Job Dependencies
Jobs can trigger dependent jobs after successful completion using `dependent_job_ids` field.

---

**Version:** 1.0.0  
**Last Updated:** 2026-04-26  
**Build:** Complete (40+ features)
