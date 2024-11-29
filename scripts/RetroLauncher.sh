#!/bin/sh
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"
JLINK_VM_OPTIONS="-Dhttps.protocols=TLSv1.2,TLSv1.1,TLSv1"
"$SCRIPT_DIR/bin/java" $JLINK_VM_OPTIONS -m com.karandaev.retrolauncher/com.karandaev.retrolauncher.Main "$@" >/dev/null 2>&1 &
