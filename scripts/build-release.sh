#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

RUN_TESTS=1
ALLOW_DIRTY=0
NEXT_VERSION_NAME=""
NEXT_VERSION_CODE=""

usage() {
  cat <<'USAGE'
Сборка подписанного release APK.

Использование:
  ./scripts/build-release.sh
  ./scripts/build-release.sh --version 1.0.5
  ./scripts/build-release.sh --version 1.0.5 --code 5
  ./scripts/build-release.sh --skip-tests
  ./scripts/build-release.sh --allow-dirty

Переменные окружения, если нужно переопределить подпись:
  ANDROID_KEYSTORE_PATH
  ANDROID_KEYSTORE_PASSWORD
  ANDROID_KEYSTORE_PASSWORD_FILE
  ANDROID_KEY_ALIAS
  ANDROID_KEY_PASSWORD
  ANDROID_KEY_PASSWORD_FILE
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --version)
      NEXT_VERSION_NAME="${2:-}"
      shift 2
      ;;
    --code)
      NEXT_VERSION_CODE="${2:-}"
      shift 2
      ;;
    --skip-tests|--no-tests)
      RUN_TESTS=0
      shift
      ;;
    --allow-dirty)
      ALLOW_DIRTY=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      if [[ -z "$NEXT_VERSION_NAME" ]]; then
        NEXT_VERSION_NAME="$1"
        shift
      else
        echo "Неизвестный аргумент: $1" >&2
        usage >&2
        exit 1
      fi
      ;;
  esac
done

if [[ "$ALLOW_DIRTY" -ne 1 ]] && git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  if [[ -n "$(git status --porcelain)" ]]; then
    echo "Рабочее дерево не чистое. Закоммить изменения или запусти с --allow-dirty." >&2
    exit 1
  fi
fi

if [[ -n "$NEXT_VERSION_NAME" && ! "$NEXT_VERSION_NAME" =~ ^[0-9]+(\.[0-9]+){1,3}([-+][A-Za-z0-9._-]+)?$ ]]; then
  echo "Некорректная версия: $NEXT_VERSION_NAME" >&2
  exit 1
fi

current_version_code() {
  sed -nE 's/^[[:space:]]*versionCode[[:space:]]*=[[:space:]]*([0-9]+).*$/\1/p' app/build.gradle.kts | head -n 1
}

current_version_name() {
  sed -nE 's/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*"([^"]+)".*$/\1/p' app/build.gradle.kts | head -n 1
}

if [[ -n "$NEXT_VERSION_NAME" ]]; then
  CURRENT_CODE="$(current_version_code)"
  if [[ -z "$CURRENT_CODE" ]]; then
    echo "Не удалось прочитать versionCode из app/build.gradle.kts" >&2
    exit 1
  fi

  if [[ -z "$NEXT_VERSION_CODE" ]]; then
    NEXT_VERSION_CODE=$((CURRENT_CODE + 1))
  fi

  if [[ ! "$NEXT_VERSION_CODE" =~ ^[0-9]+$ ]]; then
    echo "Некорректный versionCode: $NEXT_VERSION_CODE" >&2
    exit 1
  fi

  sed -i -E "s/^([[:space:]]*versionCode[[:space:]]*=[[:space:]]*)[0-9]+/\1${NEXT_VERSION_CODE}/" app/build.gradle.kts
  sed -i -E "s/^([[:space:]]*versionName[[:space:]]*=[[:space:]]*)\"[^\"]+\"/\1\"${NEXT_VERSION_NAME}\"/" app/build.gradle.kts
fi

VERSION_NAME="$(current_version_name)"
if [[ -z "$VERSION_NAME" ]]; then
  echo "Не удалось прочитать versionName из app/build.gradle.kts" >&2
  exit 1
fi

GRADLE_CMD="${GRADLE_CMD:-./gradlew}"
if [[ ! -x "$GRADLE_CMD" ]]; then
  echo "Не найден исполняемый Gradle wrapper: $GRADLE_CMD" >&2
  exit 1
fi

if [[ "$RUN_TESTS" -eq 1 ]]; then
  "$GRADLE_CMD" :app:testDebugUnitTest
fi
"$GRADLE_CMD" :app:assembleRelease

SDK_DIR="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [[ -z "$SDK_DIR" && -f local.properties ]]; then
  SDK_DIR="$(sed -nE 's/^sdk\.dir=(.*)$/\1/p' local.properties | head -n 1)"
fi
if [[ -z "$SDK_DIR" ]]; then
  SDK_DIR="$HOME/Android/Sdk"
fi

BUILD_TOOLS_DIR="$(find "$SDK_DIR/build-tools" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | sort -V | tail -n 1)"
ZIPALIGN="$BUILD_TOOLS_DIR/zipalign"
APKSIGNER="$BUILD_TOOLS_DIR/apksigner"

if [[ ! -x "$ZIPALIGN" || ! -x "$APKSIGNER" ]]; then
  echo "Не найдены zipalign/apksigner в Android SDK: $SDK_DIR" >&2
  exit 1
fi

KEYSTORE_PATH="${ANDROID_KEYSTORE_PATH:-$HOME/.android/savings-calendar-release.jks}"
KEY_ALIAS="${ANDROID_KEY_ALIAS:-savings-calendar}"

if [[ ! -f "$KEYSTORE_PATH" ]]; then
  echo "Не найден keystore: $KEYSTORE_PATH" >&2
  exit 1
fi

KS_PASS_ARGS=()
if [[ -n "${ANDROID_KEYSTORE_PASSWORD:-}" ]]; then
  KS_PASS_ARGS=(--ks-pass "pass:${ANDROID_KEYSTORE_PASSWORD}")
elif [[ -n "${ANDROID_KEYSTORE_PASSWORD_FILE:-}" ]]; then
  KS_PASS_ARGS=(--ks-pass "file:${ANDROID_KEYSTORE_PASSWORD_FILE}")
elif [[ -f "$HOME/.android/savings-calendar-release.storepass" ]]; then
  KS_PASS_ARGS=(--ks-pass "file:$HOME/.android/savings-calendar-release.storepass")
else
  echo "Не задан пароль keystore. Укажи ANDROID_KEYSTORE_PASSWORD или ANDROID_KEYSTORE_PASSWORD_FILE." >&2
  exit 1
fi

KEY_PASS_ARGS=()
if [[ -n "${ANDROID_KEY_PASSWORD:-}" ]]; then
  KEY_PASS_ARGS=(--key-pass "pass:${ANDROID_KEY_PASSWORD}")
elif [[ -n "${ANDROID_KEY_PASSWORD_FILE:-}" ]]; then
  KEY_PASS_ARGS=(--key-pass "file:${ANDROID_KEY_PASSWORD_FILE}")
fi

mkdir -p release

UNSIGNED_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
ALIGNED_APK="release/savings-calendar-${VERSION_NAME}-aligned.apk"
SIGNED_APK="release/savings-calendar-${VERSION_NAME}.apk"

if [[ ! -f "$UNSIGNED_APK" ]]; then
  echo "Не найден unsigned APK: $UNSIGNED_APK" >&2
  exit 1
fi

rm -f "$ALIGNED_APK" "$SIGNED_APK" "$SIGNED_APK.idsig"

"$ZIPALIGN" -p -f 4 "$UNSIGNED_APK" "$ALIGNED_APK"
"$APKSIGNER" sign \
  --ks "$KEYSTORE_PATH" \
  --ks-key-alias "$KEY_ALIAS" \
  "${KS_PASS_ARGS[@]}" \
  "${KEY_PASS_ARGS[@]}" \
  --out "$SIGNED_APK" \
  "$ALIGNED_APK"
"$APKSIGNER" verify --verbose "$SIGNED_APK"

echo
echo "Готово: $SIGNED_APK"
echo "SHA-256: $(sha256sum "$SIGNED_APK" | awk '{print $1}')"
