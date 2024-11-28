#!/bin/sh
JLINK_VM_OPTIONS=
bin/java $JLINK_VM_OPTIONS -m com.karandaev.retrolauncher/com.karandaev.retrolauncher.Main "$@" >/dev/null 2>&1 &