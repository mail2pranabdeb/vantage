# Vantage Admin Platform

[![Architecture](https://img.shields.io/badge/Architecture-Modular%20Monolith-blue.svg)](https://spring.io/projects/spring-boot)
[![UI](https://img.shields.io/badge/UI-React%2019%20%2B%20Vite-61dafb.svg)](https://react.dev/)
[![Database](https://img.shields.io/badge/Database-Database%20Agnostic-green.svg)](https://www.h2database.com/)

A premium, enterprise-grade business management system built with **Spring Boot 4** and **React 19**. Featuring a state-of-the-art **Glassmorphism UI**, real-time monitoring via WebSockets, and a unified technical control center.

---

## ⚡ Quick Start

### Prerequisites
- **Java 21+** (Required)
- **Node.js 22.12+** (Required for Vite 7.x)
- **Maven 3.8+**

### 1. Build
```bash
# Windows
mvnw.cmd clean compile

# Linux/macOS
./mvnw clean compile
```

### 2. Run
```bash
cd vantage-admin
mvnw.cmd spring-boot:run
```

- **App URL**: [http://localhost:8081](http://localhost:8081)
- **H2 Console**: [http://localhost:8081/h2-console](http://localhost:8081/h2-console)
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
  port: 8081

spring:
  datasource:
    url: jdbc:h2:file:D:/Projects/vantage-master/data/vantage;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1
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

---

## 🛠 Technical Architecture

### Backend Stack
- **Spring Boot 4.0.x**: Core framework
- **Spring Security**: RBAC and method-level security
- **Spring Data JPA**: Database-agnostic persistence
- **Quartz Scheduler**: Enterprise scheduling
- **WebSocket (STOMP)**: Real-time event propagation
- **Lombok**: Reduced boilerplate code

### Frontend Stack
- **React 19**: Modern component architecture
- **Vite**: Ultra-fast build tool
- **Glassmorphism Theme**: Custom CSS design system
- **Lucide Icons**: High-fidelity iconography

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

---

## 🚀 Future Roadmap
- [x] **Mobile Dashboard**: PWA support added
- [ ] **AI-Powered Insights**: Anomaly detection in job execution patterns
- [ ] **Gantt Timeline**: Visualizer for complex job dependency chains
- [ ] **PDF Previewer**: Integrated browser-based PDF report viewer

---

**Built with ❤️ by the Vantage Development Team**