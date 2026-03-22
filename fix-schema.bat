@echo off
setlocal enabledelayedexpansion
set "inputFile=vantage-admin\src\main\resources\schema.sql"
set "tempFile=vantage-admin\src\main\resources\schema.sql.tmp"

(
  for /f "delims=" %%a in ('type "%inputFile%"') do (
    set "line=%%a"
    set "line=!line:varvarchar=varchar!"
    echo(!line!
  )
) > "%tempFile%"

move /y "%tempFile%" "%inputFile%" >nul
del "%inputFile%.tmp" 2>nul
echo Done!
