#!/usr/bin/env bash
set -e

if [ ! -f bin/agentflow/Main.class ]; then
    echo "[INFO] Binaries not found. Building project..."
    ./build.sh
fi

java -Dfile.encoding=UTF-8 -cp bin agentflow.Main "$@"
