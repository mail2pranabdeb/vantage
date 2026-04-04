@echo off
echo ========================================
echo Fix Login Schema for H2 Database
echo ========================================
echo.
echo This script will apply schema fixes to the H2 database.
echo Make sure the server is NOT running before executing this.
echo.
pause

echo.
echo Applying schema fixes...
java -cp vantage-admin\target\vantage-admin-*.jar;vantage-framework\target\vantage-framework-*.jar;vantage-common\target\vantage-common-*.jar ^
  org.h2.tools.RunScript -url jdbc:h2:file:.\data\vantage;MODE=Oracle -user sa -script fix-login-schema.sql

echo.
echo Schema fix applied successfully!
echo You can now start the server.
echo.
pause
