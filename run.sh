#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p target/classes target/test-classes
javac -d target/classes src/*.java
case "${1:-demo}" in
  test) javac -cp target/classes -d target/test-classes tests/*.java
        java -cp target/classes:target/test-classes AlgorithmTests ;;
  demo) java -cp target/classes Main ;;
  experiment) java -cp target/classes Main experiment ;;
  *) echo "Usage: sh run.sh [demo|test|experiment]" >&2; exit 2 ;;
esac
