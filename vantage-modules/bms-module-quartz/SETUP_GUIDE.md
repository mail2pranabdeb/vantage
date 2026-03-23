# Job Module Enhancement - Setup Guide

## 🎉 Complete Feature List

### ✅ Implemented Features (23/28)

#### Job Configuration from UI
- ✅ Complete job setup form with all options
- ✅ Cron expression builder with presets
- ✅ Retry mechanism configuration
- ✅ Timeout settings
- ✅ Dependency configuration
- ✅ Time zone selection
- ✅ Holiday execution toggle

#### Live Monitoring
- ✅ **Live Job Logs Viewer** - Real-time log streaming
- ✅ WebSocket-based live updates
- ✅ Filter by status (All/Success/Failed/Running)
- ✅ Search functionality
- ✅ Auto-scroll toggle
- ✅ Export logs to JSON
- ✅ Detailed log view with exception stack traces

#### Email Template Management
- ✅ **Email Template Manager** - Full CRUD from UI
- ✅ HTML email templates with variables
- ✅ Visual preview
- ✅ Quick variable insertion
- ✅ Default template selection
- ✅ Active/Inactive toggle
- ✅ 3 pre-built templates (Failure, Success, Recovery)

---

## 📁 New Files Created

### Backend (Java)
```
vantage-modules/bms-module-quartz/src/main/java/com/pd/modules/quartz/
├── domain/
│   ├── EmailTemplate.java                    [NEW]
│   ├── SysJob.java                           [ENHANCED]
│   └── SysJobLog.java                        [ENHANCED]
├── infrastructure/repository/
│   ├── EmailTemplateRepository.java          [NEW]
│   ├── SysJobLogRepository.java              [ENHANCED]
│   └── SysJobRepository.java                 [EXISTING]
├── service/
│   ├── EmailTemplateService.java             [NEW]
│   ├── JobDependencyService.java             [NEW]
│   ├── JobMetricsService.java                [NEW]
│   ├── JobNotificationService.java           [NEW]
│   ├── JobTemplateService.java               [NEW]
│   ├── JobWebSocketService.java              [NEW]
│   └── impl/SysJobServiceImpl.java           [ENHANCED]
└── controller/
    ├── EmailTemplateController.java          [NEW]
    ├── JobDashboardController.java           [NEW]
    ├── JobLogController.java                 [NEW]
    ├── JobTemplateController.java            [NEW]
    └── SysJobController.java                 [ENHANCED]
```

### Framework Configuration
```
vantage-framework/src/main/java/com/pd/framework/config/
├── NotificationConfig.java                   [NEW]
└── WebSocketConfig.java                      [NEW]
```

### Frontend (React)
```
vantage-admin/vantage-ui/src/
├── components/
│   └── CronBuilder.jsx                       [NEW]
└── pages/
    ├── JobList.jsx                           [ENHANCED]
    ├── LiveJobLogs.jsx                       [NEW]
    └── EmailTemplateManager.jsx              [NEW]
```

### Database Migration
```
vantage-modules/bms-module-quartz/src/main/resources/migration/
├── job-enhancement-migration.sql             [NEW]
└── email-template-migration.sql              [NEW]
```

---

## 🚀 Setup Instructions

### Step 1: Database Migration

Run the SQL migration scripts in order:

```sql
-- 1. Add new columns to existing tables
source vantage-modules/bms-module-quartz/src/main/resources/migration/job-enhancement-migration.sql

-- 2. Create email template table with default data
source vantage-modules/bms-module-quartz/src/main/resources/migration/email-template-migration.sql
```

Or execute directly via your database client.

### Step 2: Install UI Dependencies

```bash
cd vantage-admin/vantage-ui
npm install
```

This will install:
- `@stomp/stompjs` - WebSocket client for real-time updates
- `sockjs-client` - WebSocket fallback support

### Step 3: Configure Email (Optional)

Add to your `application.yml` or `application.properties`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

app:
  name: Vantage
```

### Step 4: Build and Run

```bash
# From project root
mvn clean install

# Run the application
cd vantage-admin
mvn spring-boot:run
```

---

## 🌐 New UI Pages

### 1. Enhanced Job Management
**Route:** `/system/job` (existing, enhanced)

**New Features:**
- Metrics dashboard at top
- Real-time updates via WebSocket
- Advanced job configuration modal
- Cron builder
- Template selection
- Bulk operations
- Import/Export

### 2. Live Job Logs
**Route:** Add to your router configuration

```jsx
// In your App.jsx or router file
import LiveJobLogs from './pages/LiveJobLogs';

// Add route
<Route path="/system/job-logs" element={<LiveJobLogs />} />
```

**Features:**
- Real-time log streaming
- Filter by status
- Search logs
- Auto-scroll
- Export functionality
- Detailed exception view

### 3. Email Template Manager
**Route:** Add to your router configuration

```jsx
// In your router
import EmailTemplateManager from './pages/EmailTemplateManager';

<Route path="/system/email-templates" element={<EmailTemplateManager />} />
```

**Features:**
- Create/Edit/Delete templates
- HTML editor with preview
- Variable insertion
- Default template management
- Active/Inactive toggle

---

## 📡 API Endpoints

### Email Templates
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/email-template/list` | List all templates |
| GET | `/api/system/email-template/active` | List active templates |
| GET | `/api/system/email-template/{id}` | Get by ID |
| GET | `/api/system/email-template/type/{type}` | Get by type |
| POST | `/api/system/email-template` | Create template |
| PUT | `/api/system/email-template` | Update template |
| DELETE | `/api/system/email-template/{id}` | Delete template |
| PUT | `/api/system/email-template/{id}/set-default` | Set as default |
| PUT | `/api/system/email-template/{id}/toggle-active` | Toggle active |
| GET | `/api/system/email-template/{id}/preview` | Preview template |

### Job Logs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/job-log/list` | List all logs |
| GET | `/api/system/job-log/job/{jobId}` | Logs by job |
| GET | `/api/system/job-log/failed/recent` | Recent failures |
| GET | `/api/system/job-log/statistics` | Statistics |
| DELETE | `/api/system/job-log/{id}` | Delete log |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/job-dashboard/metrics` | Dashboard metrics |
| GET | `/api/system/job-dashboard/trend` | Execution trends |

---

## 🔌 WebSocket Integration

### Connection Setup
```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
    webSocketFactory: () => new SockJS('/ws-job'),
    reconnectDelay: 5000,
    onConnect: () => {
        console.log('Connected');
        
        // Subscribe to job updates
        client.subscribe('/topic/job/updates', (message) => {
            const event = JSON.parse(message.body);
            console.log('Event:', event);
        });
    }
});

client.activate();
```

### Event Types
- `JOB_CREATED` - New job created
- `JOB_DELETED` - Job deleted
- `JOB_STARTED` - Execution started
- `JOB_COMPLETED` - Execution finished
- `JOB_FAILED` - Execution failed
- `JOB_STATUS_CHANGED` - Status changed

---

## 📧 Email Template Variables

Use these variables in your email templates:

| Variable | Description |
|----------|-------------|
| `${appName}` | Application name |
| `${jobId}` | Job ID |
| `${jobName}` | Job name |
| `${jobGroup}` | Job group |
| `${invokeTarget}` | Invoke target method |
| `${cronExpression}` | Cron expression |
| `${executionTime}` | Execution start time |
| `${duration}` | Execution duration (ms) |
| `${retryCount}` | Retry count |
| `${status}` | Execution status (Success/Failed) |
| `${message}` | Result message |
| `${exceptionInfo}` | Exception stack trace |
| `${timestamp}` | Current timestamp |

---

## 🎨 UI Navigation Setup

Add these routes to your React application:

```jsx
// App.jsx or router file
import { Routes, Route } from 'react-router-dom';
import JobList from './pages/JobList';
import LiveJobLogs from './pages/LiveJobLogs';
import EmailTemplateManager from './pages/EmailTemplateManager';

function App() {
    return (
        <Routes>
            {/* Existing routes */}
            <Route path="/system/job" element={<JobList />} />
            <Route path="/system/job-logs" element={<LiveJobLogs />} />
            <Route path="/system/email-templates" element={<EmailTemplateManager />} />
        </Routes>
    );
}
```

Add menu items to your sidebar:

```jsx
// In your sidebar/menu component
<nav>
    {/* ... other menu items */}
    <MenuItem to="/system/job" icon={<Clock />}>Job Management</MenuItem>
    <MenuItem to="/system/job-logs" icon={<Terminal />}>Live Logs</MenuItem>
    <MenuItem to="/system/email-templates" icon={<Mail />}>Email Templates</MenuItem>
</nav>
```

---

## ✅ Testing Checklist

- [ ] Run database migrations
- [ ] Install npm dependencies
- [ ] Start the application
- [ ] Navigate to Job Management page
- [ ] Create a new job with all settings
- [ ] Test cron builder
- [ ] Test job execution
- [ ] Navigate to Live Logs page
- [ ] Verify real-time updates
- [ ] Navigate to Email Templates page
- [ ] Create custom template
- [ ] Test template preview
- [ ] Configure email settings
- [ ] Test email notification on job failure

---

## 🐛 Troubleshooting

### WebSocket Not Connecting
- Check if `/ws-job` endpoint is accessible
- Verify WebSocket configuration in `WebSocketConfig.java`
- Check browser console for errors

### Email Not Sending
- Verify SMTP configuration
- Check email credentials
- Ensure `notifyOnFailure` is enabled for the job
- Check application logs for email errors

### Templates Not Loading
- Run email template migration script
- Verify `EmailTemplate` entity is scanned
- Check database table exists

---

## 📝 Next Steps

### Pending Features (5 remaining)
1. Job Calendar View
2. Gantt Chart Timeline
3. Role-based Access Control
4. Job Versioning
5. Holiday Calendar Integration

These can be implemented based on priority.

---

## 📚 Documentation

- `JOB_ENHANCEMENTS.md` - Complete feature documentation
- `README.md` - Project overview
- API documentation available at `/swagger-ui.html` (if Swagger is configured)

---

**Version:** 1.0.0  
**Last Updated:** 2026-03-22  
**Author:** Vantage Development Team
