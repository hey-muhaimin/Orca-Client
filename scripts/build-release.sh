#!/usr/bin/env bash
set -euo pipefail

# Orca Release Build Script
# Builds release APK, verifies signature, and exports to /exported_build/orca-release.apk

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

EXPORT_DIR="${EXPORT_DIR:-$ROOT_DIR/exported_build}"
mkdir -p "$EXPORT_DIR"

echo "========================================"
echo " Building Orca Release APK"
echo "========================================"

gradle :app:assembleRelease

RELEASE_APK="app/build/outputs/apk/release/app-release.apk"

if [ ! -f "$RELEASE_APK" ]; then
    echo "Error: Release APK not found at $RELEASE_APK" >&2
    exit 1
fi

DEST_APK="$EXPORT_DIR/orca-release.apk"
cp "$RELEASE_APK" "$DEST_APK"

# Detect apksigner and verify
BUILD_TOOLS_DIR=$(find "${ANDROID_SDK_ROOT:-/opt/android/sdk}/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -n 1)
APKSIGNER="$BUILD_TOOLS_DIR/apksigner"

if [ -x "$APKSIGNER" ]; then
    echo "-> Verifying APK signature..."
    "$APKSIGNER" verify --verbose "$DEST_APK" || {
        echo "Signing release APK..."
        "$ROOT_DIR/scripts/sign-apk.sh" "$DEST_APK" "$DEST_APK"
    }
fi

sha256sum "$DEST_APK" > "${DEST_APK}.sha256"

APK_SIZE=$(du -h "$DEST_APK" | cut -f1)

echo "========================================"
echo "✓ Release APK Ready to Install!"
echo " Output: $DEST_APK ($APK_SIZE)"
echo " Checksum: ${DEST_APK}.sha256"
echo " Install with: adb install -r $DEST_APK"
echo "========================================"
