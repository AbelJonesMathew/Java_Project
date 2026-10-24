@echo off
title CampusVoice - Automated Launcher
echo ========================================================
echo        CAMPUSVOICE - SJCET AUTONOMOUS REDRESSAL        
echo ========================================================

if not defined JAVA_HOME (
    for /d %%D in ("C:\Program Files\Java\jdk*") do if exist "%%D\bin\javac.exe" set "JAVA_HOME=%%D"
    if not defined JAVA_HOME (
        for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk*") do if exist "%%D\bin\javac.exe" set "JAVA_HOME=%%D"
    )
)

cd /d "%~dp0campusvoice-java"
call run.bat
