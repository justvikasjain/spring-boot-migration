@echo off
setlocal enabledelayedexpansion

echo === Database Migration Tool - Build and Run Script ===

REM Check Java version
echo Checking Java version...
java -version 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Java not found. Please install JDK 17 or higher.
    exit /b 1
)

REM Build the application
echo Building the application...
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo Build failed. Please check the errors above.
    exit /b 1
)

echo Build successful!

REM Run the application
echo Starting the application...
echo The application will be available at http://localhost:8080
java -jar target\data-migration-0.0.1-SNAPSHOT.jar

exit /b 0
