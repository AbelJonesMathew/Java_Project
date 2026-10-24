@echo off
setlocal
if not defined JAVA_HOME (
    for /d %%D in ("C:\Program Files\Java\jdk*") do if exist "%%D\bin\javac.exe" set "JAVA_HOME=%%D"
    if not defined JAVA_HOME (
        for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk*") do if exist "%%D\bin\javac.exe" set "JAVA_HOME=%%D"
    )
)
powershell -ExecutionPolicy Bypass -File "%~dp0run.ps1"
if %ERRORLEVEL% neq 0 pause
