# Job Module Enhancements

Comprehensive enhancements to the Vantage Job Management Module with advanced scheduling, monitoring, and reliability features.

## Table of Contents

1. [Features Overview](#features-overview)
2. [Backend Enhancements](#backend-enhancements)
3. [Frontend Enhancements](#frontend-enhancements)
4. [API Reference](#api-reference)
5. [Database Migration](#database-migration)
6. [Configuration](#configuration)

---

## Features Overview

### 1. Job Execution & Reliability
- ✅ **Execution History Logging** - Database persistence of all job executions
- ✅ **Retry Mechanism** - Configurable auto-retry for failed jobs
- ✅ **Job Timeout** - Maximum execution time protection
- ✅ **Email/Webhook Notifications** - Alerts on job failures
- ✅ **Job Dependencies** - Chain jobs together

### 2. Monitoring & Observability
- ✅ **Dashboard Metrics** - Success rate, avg execution time, job counts
- ✅ **Real-time Status Updates** - WebSocket-based live updates
- ✅ **Execution Logs** - Detailed log viewing per job
- ✅ **Performance Analytics** - 30-day trend analysis

### 3. Job Management Features
- ✅ **Job Templates** - Pre-defined configurations for common tasks
- ✅ **Bulk Operations** - Pause/resume/delete multiple jobs
- ✅ **Import/Export** - JSON-based job configuration backup
- ✅ **Cron Expression Builder** - Visual cron builder with presets
- ✅ **Job Groups** - Organize jobs by category

### 4. Advanced Scheduling
- ✅ **Time Zone Support** - Schedule in different time zones
- ✅ **Holiday Calendar** - Skip execution on holidays (configurable)
- ✅ **Dependent Jobs** - Trigger downstream jobs automatically

### 5. Integration Capabilities
- ✅ **Webhook Support** - External system notifications
- ✅ **Email Notifications** - Failure alerts via email
- ✅ **WebSocket Events** - Real-time UI updates
- ✅ **REST API** - Complete API for all operations

---

## Backend Enhancements

### New Domain Classes

#### SysJob (Enhanced)
```java
// New fields added:
- maxRetryCount: Integer      // Auto-retry count
- retryInterval: Integer       // Seconds between retries
- timeoutSeconds: Integer      // Max execution time
- notifyOnFailure: Boolean     // Enable notifications
- notificationEmails: String   // Comma-separated emails
- webhookUrl: String          // Webhook endpoint
- dependentJobIds: String     // Dependent job IDs
- timeZone: String            // Scheduling timezone
- allowHoliday: Boolean       // Holiday execution flag
- templateName: String        // Template reference
```

#### SysJobLog (Enhanced)
```java
// Now a JPA entity with:
- jobId: Long                 // Reference to job
- executionDuration: Long     // Milliseconds
- retryCount: Integer         // Actual retries
- createTime: LocalDateTime   // Record creation
```

### New Services

| Service | Description |
|---------|-------------|
| `JobNotificationService` | Email and webhook notifications |
| `JobDependencyService` | Handle job dependencies |
| `JobMetricsService` | Dashboard statistics |
| `JobWebSocketService` | Real-time WebSocket updates |
| `JobTemplateService` | Job template management |

### New Controllers

| Controller | Endpoint | Description |
|------------|----------|-------------|
| `JobLogController` | `/api/system/job-log` | Execution logs |
| `JobDashboardController` | `/api/system/job-dashboard` | Metrics & analytics |
| `JobTemplateController` | `/api/system/job-template` | Template management |

---

## Frontend Enhancements

### New Components

#### CronBuilder.jsx
Visual cron expression builder with:
- Quick preset buttons
- Simple/Advanced modes
- Real-time preview
- Field-specific editors

### Enhanced JobList.jsx

Features:
- **Metrics Dashboard** - Summary cards at top
- **Real-time Updates** - WebSocket integration
- **Advanced Modal** - Tabbed interface for settings
- **Job Logs View** - Execution history per job
- **Templates Modal** - Quick job creation
- **Metrics Modal** - Detailed statistics
- **Bulk Actions** - Multi-select operations
- **Import/Export** - JSON file handling

### New Dependencies
```json
{
  "@stomp/stompjs": "^7.1.1",
  "sockjs-client": "^1.6.1"
}
```

---

## API Reference

### Job Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/job/list` | List all jobs |
| GET | `/api/system/job/{id}` | Get job by ID |
| POST | `/api/system/job` | Create job |
| PUT | `/api/system/job` | Update job |
| DELETE | `/api/system/job/{id}` | Delete job |
| DELETE | `/api/system/job/batch` | Bulk delete |
| PUT | `/api/system/job/batch/pause` | Bulk pause |
| PUT | `/api/system/job/batch/resume` | Bulk resume |
| POST | `/api/system/job/batch/run` | Bulk execute |
| GET | `/api/system/job/export` | Export jobs |
| POST | `/api/system/job/import` | Import jobs |

### Job Logs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/job-log/list` | List all logs |
| GET | `/api/system/job-log/job/{jobId}` | Logs by job |
| GET | `/api/system/job-log/{logId}` | Log by ID |
| GET | `/api/system/job-log/failed/recent` | Recent failures |
| GET | `/api/system/job-log/statistics` | Log statistics |
| DELETE | `/api/system/job-log/{logId}` | Delete log |

### Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/job-dashboard/metrics` | Dashboard metrics |
| GET | `/api/system/job-dashboard/trend` | Execution trends |

### Templates

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/job-template/list` | List templates |
| GET | `/api/system/job-template/{name}` | Get template |
| POST | `/api/system/job-template/create/{name}` | Create from template |

---

## Database Migration

Run the migration script to add new columns:

```sql
-- Location: src/main/resources/migration/job-enhancement-migration.sql

-- Add columns to sys_job
ALTER TABLE sys_job ADD COLUMN max_retry_count INTEGER DEFAULT 0;
ALTER TABLE sys_job ADD COLUMN retry_interval INTEGER DEFAULT 60;
ALTER TABLE sys_job ADD COLUMN timeout_seconds INTEGER DEFAULT 3600;
-- ... (see migration file for complete script)
```

---

## Configuration

### Email Notifications

Add to `application.yml`:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

app:
  name: Vantage
```

### WebSocket Configuration

WebSocket endpoint: `/ws-job`
Subscription topic: `/topic/job/updates`

### Job Templates

Pre-defined templates available:
- `daily-backup` - Daily backup at 2 AM
- `hourly-cleanup` - Hourly cleanup task
- `weekly-report` - Weekly report (Monday 8 AM)
- `monthly-analytics` - Monthly analytics (1st day 6 AM)
- `cache-refresh` - Cache refresh (every 30 min)
- `notification-check` - Notification checker (every 15 min)

---

## Usage Examples

### Create Job with Retry
```json
{
  "jobName": "Data Sync",
  "jobGroup": "system",
  "invokeTarget": "syncService.performSync()",
  "cronExpression": "0 */30 * * * ?",
  "maxRetryCount": 3,
  "retryInterval": 60,
  "timeoutSeconds": 300,
  "notifyOnFailure": true,
  "notificationEmails": "admin@example.com"
}
```

### Create Job with Dependencies
```json
{
  "jobName": "Report Generator",
  "jobGroup": "report",
  "invokeTarget": "reportService.generate()",
  "cronExpression": "0 0 9 * * ?",
  "dependentJobIds": "5, 6",
  "notifyOnFailure": true,
  "webhookUrl": "https://hooks.slack.com/..."
}
```

---

## Event Types (WebSocket)

| Event | Description |
|-------|-------------|
| `JOB_CREATED` | New job created |
| `JOB_DELETED` | Job deleted |
| `JOB_STARTED` | Job execution started |
| `JOB_COMPLETED` | Job execution finished |
| `JOB_FAILED` | Job execution failed |
| `JOB_STATUS_CHANGED` | Job status changed |

---

## Future Enhancements (Pending)

- [ ] Job Calendar View
- [ ] Gantt Chart Timeline
- [ ] Role-based Access Control
- [ ] Audit Logging
- [ ] Multiple Triggers per Job
- [ ] Holiday Calendar Integration
- [ ] Dynamic Scheduling Rules
- [ ] Job Versioning

---

## Support

For issues or questions, please refer to the main Vantage documentation or contact the development team.
