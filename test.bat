@echo off
setlocal enabledelayedexpansion

:: Locate java & javac
where javac >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\javac.exe" (
        set "PATH=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin;%PATH%"
    ) else if defined JAVA_HOME (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    )
)

call build.bat
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%

echo Compiling test suite...
dir /s /B src\test\java\*.java > test_sources.txt
javac -encoding UTF-8 -cp bin -d bin @test_sources.txt
del test_sources.txt

echo Running automated verification tests...
chcp 65001 >nul 2>&1
java -Dfile.encoding=UTF-8 -cp bin agentflow.WorkflowVerificationTest
