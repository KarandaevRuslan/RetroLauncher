@echo off
set JLINK_VM_OPTIONS=
start "" "bin\javaw" %JLINK_VM_OPTIONS% -m com.karandaev.retrolauncher/com.karandaev.retrolauncher.Main %* && exit 0
