#!/usr/bin/env bash
set -e

echo "======================================================="
echo "  Building Modular Agentic Workflow System (Java 17)"
echo "======================================================="

mkdir -p bin
find src/main/java -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
rm -f sources.txt

echo "[SUCCESS] Compilation completed into bin/ directory."
