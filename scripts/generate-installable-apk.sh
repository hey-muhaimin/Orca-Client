#!/usr/bin/env bash
set -euo pipefail

# Orca - Turnkey Generate Ready-To-Install APK
# Builds, aligns, signs, and places ready-to-install APK directly into /exported_build/orca-installable.apk

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

EXPORT_DIR="${EXPORT_DIR:-$ROOT_DIR/exported_build}"
mkdir -p "$EXPORT_DIR"

INSTALLABLE_APK="$EXPORT_DIR/orca-installable.apk"

echo "=========================================================="
echo "  Orca - Generating Ready-to-Install APK"
echo "  Target Output: $INSTALLABLE_APK"
echo "=========================================================="

# 1. Build
echo "-> [1/4] Assembling release APK..."
gradle :app:assembleRelease

RAW_APK="app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$RAW_APK" ]; then
    RAW_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
fi

# 2. Sign and Align
echo "-> [2/4] Signing and verifying APK..."
"$ROOT_DIR/scripts/sign-apk.sh" "$RAW_APK" "$INSTALLABLE_APK"

# 3. Checksums
echo "-> [3/4] Generating checksums..."
sha256sum "$INSTALLABLE_APK" > "${INSTALLABLE_APK}.sha256"
md5sum "$INSTALLABLE_APK" > "${INSTALLABLE_APK}.md5"

# 4. Summary
echo "-> [4/4] Finalizing build bundle..."
APK_SIZE=$(du -h "$INSTALLABLE_APK" | cut -f1)
SHA256=$(cat "${INSTALLABLE_APK}.sha256" | cut -d' ' -f1)

echo "=========================================================="
echo "  ✓ READY-TO-INSTALL APK GENERATED SUCCESSFULLY!"
echo "=========================================================="
echo "  APK Path:    $INSTALLABLE_APK"
echo "  Size:        $APK_SIZE"
echo "  SHA-256:     $SHA256"
echo ""
echo "  To install directly on an Android device or emulator:"
echo "    adb install -r $INSTALLABLE_APK"
echo "=========================================================="
