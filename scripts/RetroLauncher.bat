@echo off
set "SCRIPT_DIR=%~dp0"
set "JLINK_VM_OPTIONS=-Dhttps.protocols=TLSv1.2,TLSv1.1,TLSv1"
start "" "%SCRIPT_DIR%bin\javaw.exe" %JLINK_VM_OPTIONS% -m com.karandaev.retrolauncher/com.karandaev.retrolauncher.Main %*
exit /b 0
