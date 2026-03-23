@echo off
echo ================================================
echo   Vantage Admin - Stop Script
echo ================================================
echo.

echo Stopping Vantage Admin Application...
taskkill /F /FI "IMAGENAME eq java.exe" 2>nul

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Application stopped successfully!
) else (
    echo.
    echo No Java processes found running.
)

echo.
pause
