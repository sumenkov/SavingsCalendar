#!/usr/bin/env bash
set -euo pipefail

if command -v ./gradlew >/dev/null 2>&1 && [[ -x ./gradlew ]]; then
  ./gradlew :app:testDebugUnitTest
elif command -v gradle >/dev/null 2>&1; then
  gradle :app:testDebugUnitTest
else
  echo "Gradle не найден. Открой проект в Android Studio или установи Gradle." >&2
  exit 1
fi
