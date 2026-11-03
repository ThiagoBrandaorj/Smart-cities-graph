#!/bin/sh
# Compila e executa o SmartNet Analyzer (Linux/macOS). Requer JDK 17+.
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -encoding UTF-8 -d out src/*.java || exit 1
java -cp out src.Main
