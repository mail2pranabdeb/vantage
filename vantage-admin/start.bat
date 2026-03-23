@echo off
echo ================================================
echo   Vantage Admin - Startup Script
echo ================================================
echo.

echo [1/3] Stopping existing Java processes...
taskkill /F /FI "IMAGENAME eq java.exe" 2>nul
timeout /t 2 /nobreak >nul

echo [2/3] Deleting old database files...
rmdir /s /q "data" 2>nul
mkdir "data" 2>nul
echo Database directory recreated.

echo [3/3] Starting Vantage Admin Application...
echo.
echo Wait for application to start (approx 35 seconds)...
echo.

start /B java -Xms512m -Xmx1024m -jar target\vantage-admin-0.0.1-SNAPSHOT.jar > startup.log 2>&1

timeout /t 35 /nobreak >nul

echo.
echo ================================================
echo   Application Started Successfully!
echo ================================================
echo.
echo Access URLs:
echo   - Main App:      http://localhost:8081
echo   - H2 Console:    http://localhost:8081/h2-console
echo   - Job Mgmt:      http://localhost:8081/system/job
echo   - Live Logs:     http://localhost:8081/system/job-logs
echo   - Email Templates: http://localhost:8081/system/email-templates
echo.
echo Login Credentials:
echo   Username: admin
echo   Password: 123456
echo.
echo ================================================
pause
