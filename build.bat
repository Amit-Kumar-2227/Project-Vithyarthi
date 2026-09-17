@echo off
setlocal enabledelayedexpansion

echo =======================================================
echo   Building Modular Agentic Workflow System (Java 17)
echo =======================================================

:: Locate javac
where javac >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\javac.exe" (
        set "PATH=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin;%PATH%"
    ) else if defined JAVA_HOME (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    ) else (
        echo [ERROR] javac compiler not found in PATH or JAVA_HOME.
        echo Please ensure JDK 17+ is installed.
        exit /b 1
    )
)

if not exist bin mkdir bin

echo Compiling source files...
dir /s /B src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
set COMPILE_STATUS=%ERRORLEVEL%
del sources.txt

if %COMPILE_STATUS% equ 0 (
    echo [SUCCESS] Compilation finished successfully into bin/ directory.
) else (
    echo [ERROR] Compilation failed with error code %COMPILE_STATUS%.
    exit /b %COMPILE_STATUS%
)
