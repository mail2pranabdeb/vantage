@echo off
rmdir /s /q "D:\Projects\vantage-master\vantage-common\src\main\java\com\bms"
if exist "D:\Projects\vantage-master\vantage-common\src\main\java\com\bms" (
    echo Failed to delete directory
    exit /b 1
)
echo Successfully deleted directory
exit /b 0
