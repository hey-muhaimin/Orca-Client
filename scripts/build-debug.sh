#!/usr/bin/env bash
set -euo pipefail

# Orca Debug Build Script
# Builds debug APK and exports to /exported_build/orca-debug.apk

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

EXPORT_DIR="${EXPORT_DIR:-$ROOT_DIR/exported_build}"
mkdir -p "$EXPORT_DIR"

echo "========================================"
echo " Building Orca Debug APK"
echo "========================================"

gradle :app:assembleDebug

DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"

if [ ! -f "$DEBUG_APK" ]; then
    echo "Error: Expected debug APK not found at $DEBUG_APK" >&2
    exit 1
fi

DEST_APK="$EXPORT_DIR/orca-debug.apk"
cp "$DEBUG_APK" "$DEST_APK"

# Generate SHA256 checksum
sha256sum "$DEST_APK" > "${DEST_APK}.sha256"

APK_SIZE=$(du -h "$DEST_APK" | cut -f1)

echo "========================================"
echo "✓ Debug APK Ready to Install!"
echo " Output: $DEST_APK ($APK_SIZE)"
echo " Checksum: ${DEST_APK}.sha256"
echo " Install with: adb install -r $DEST_APK"
echo "========================================"
