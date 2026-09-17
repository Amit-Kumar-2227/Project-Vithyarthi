@echo off
setlocal enabledelayedexpansion

:: Locate java
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\java.exe" (
        set "PATH=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin;%PATH%"
    ) else if defined JAVA_HOME (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    ) else (
        echo [ERROR] java runtime not found in PATH or JAVA_HOME.
        exit /b 1
    )
)

if not exist bin\agentflow\Main.class (
    echo [INFO] Binaries not found. Triggering build first...
    call build.bat
    if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
)

:: Enable UTF-8 code page for console output
chcp 65001 >nul 2>&1

:: Run application
java -Dfile.encoding=UTF-8 -cp bin agentflow.Main %*
