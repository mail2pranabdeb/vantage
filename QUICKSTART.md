# Vantage Admin - Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Step 1: Build & Run
```bash
# Navigate to project
cd D:\Projects\vantage-master

# Build (first time)
mvn clean install -DskipTests

# Run
cd vantage-admin
java -jar target/vantage-admin-0.0.1-SNAPSHOT.jar
```

### Step 2: Access Application
```
URL: http://localhost:8081
Username: admin
Password: 123456
```

### Step 3: Explore Key Features

#### 1️⃣ Menu Management
- Go to: **System Management → Menu Management**
- Click "Add Root Menu" to create new menu
- Click "+" on any row to add submenu
- Try creating a test menu structure

#### 2️⃣ Role Management
- Go to: **System Management → Role Management**
- Click "Add Role" to create new role
- Click Shield icon to assign permissions
- Check/uncheck menus in the tree
- Click "Save Permissions"

#### 3️⃣ Job Scheduling
- Go to: **Job Management → Job List**
- Click "Create" to create new job
- Set cron expression (e.g., `0 0/5 * * * ?` for every 5 min)
- Configure retry and timeout
- Click "Create" and watch it execute

#### 4️⃣ Report Management
- Go to: **Report Management → Report List**
- Click "Add Report"
- Enter SQL: `SELECT * FROM sys_user WHERE status = :status`
- Click "Create"
- Click Play button to execute
- Enter params: `{"status":"0"}`
- Download results as Excel!

#### 5️⃣ Code Generation
- Go to: **Tool → Code Generation**
- Select tables from grid
- Click "Generate"
- Configure package name
- Click "Generate Code"
- Download ZIP file!

### Step 4: Configure Email (Optional)
- Go to: **System Management → Config Management → Email Config**
- Enter SMTP settings
- Click "Save"
- Test by executing a report with email enabled

---

## 📋 Common Tasks

### Create a New User
1. System Management → User Management
2. Click "Add"
3. Fill in username, password, role
4. Click "Submit"

### Create a Scheduled Job
1. Job Management → Job List
2. Click "Create"
3. Set:
   - Job Name: `DataBackup`
   - Invoke Target: `backupService.backup()`
   - Cron: `0 0 2 * * ?` (daily at 2 AM)
   - Retry Count: `3`
   - Timeout: `3600`
4. Click "Create"

### Create a Report
1. Report Management → Report List
2. Click "Add Report"
3. Set:
   - Name: `User List`
   - Key: `user_list`
   - SQL: `SELECT * FROM sys_user`
   - Format: `EXCEL`
4. Click "Create"
5. Click Play to execute

### Generate Code for a Table
1. Tool → Code Generation
2. Select table (e.g., `SYS_USER`)
3. Click "Generate"
4. Set package: `com.pd.modules.user`
5. Check all components
6. Click "Generate Code"
7. Download ZIP

---

## 🔧 Quick Configuration

### Change Server Port
Edit: `vantage-admin/src/main/resources/application.yml`
```yaml
server:
  port: 8081  # Change to your port
```

### Switch Database
Edit: `vantage-admin/src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:h2:file:../data/vantage  # File-based
    # OR
    url: jdbc:h2:mem:vantage  # In-memory
```

### Enable/Disable Modules
Edit: `vantage-admin/src/main/resources/application.yml`
```yaml
module:
  init:
    system:
      enabled: true   # Enable system module
    quartz:
      enabled: true   # Enable job scheduling
    generator:
      enabled: true   # Enable code gen
```

---

## 🐛 Quick Troubleshooting

### Can't Login
```bash
# Check if admin exists
# Go to H2 Console: http://localhost:8081/h2-console
SELECT * FROM sys_user WHERE login_name='admin';
```

### Menu Not Visible
```
Solution: Logout → Login again (refreshes cache)
```

### Jobs Not Running
```
Check: Job status is "0" (active)
Check: Cron expression is valid
Check logs for errors
```

### Report Download Fails
```
Check: Popup blocker not blocking download
Check: Report has valid SQL
Test SQL in H2 Console first
```

---

## 📚 Next Steps

After getting familiar with basics:
1. Read full [DOCUMENTATION.md](DOCUMENTATION.md)
2. Explore API reference
3. Customize UI theme
4. Add custom job handlers
5. Integrate with external systems

---

**Need Help?** Check [DOCUMENTATION.md](DOCUMENTATION.md) for detailed guides.

**Happy Coding!** 🚀
