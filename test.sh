#!/usr/bin/env bash
set -e

./build.sh

echo "Compiling test suite..."
find src/test/java -name "*.java" > test_sources.txt
javac -encoding UTF-8 -cp bin -d bin @test_sources.txt
rm -f test_sources.txt

echo "Running automated verification tests..."
java -Dfile.encoding=UTF-8 -cp bin agentflow.WorkflowVerificationTest
