#!/usr/bin/env bash
set -euo pipefail

# Orca Comprehensive Build Script
# Cleans the project, assembles both debug and release APKs,
# signs and optimizes release artifacts, and saves all outputs to /exported_build.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

EXPORT_DIR="${EXPORT_DIR:-$ROOT_DIR/exported_build}"
mkdir -p "$EXPORT_DIR"

echo "=========================================================="
echo "  Orca Android Build & Packaging Pipeline"
echo "  Target Output Directory: $EXPORT_DIR"
echo "=========================================================="

# Step 1: Clean project
echo "-> [1/5] Cleaning project artifacts..."
gradle clean

# Step 2: Assemble Debug & Release APKs
echo "-> [2/5] Assembling debug and release APKs..."
gradle :app:assembleDebug :app:assembleRelease

DEBUG_SOURCE="app/build/outputs/apk/debug/app-debug.apk"
RELEASE_SOURCE="app/build/outputs/apk/release/app-release.apk"

if [ ! -f "$DEBUG_SOURCE" ]; then
    echo "Error: Debug APK was not produced at $DEBUG_SOURCE" >&2
    exit 1
fi

if [ ! -f "$RELEASE_SOURCE" ]; then
    echo "Error: Release APK was not produced at $RELEASE_SOURCE" >&2
    exit 1
fi

# Step 3: Copy and Export Debug Artifact
echo "-> [3/5] Exporting debug APK..."
DEBUG_TARGET="$EXPORT_DIR/orca-debug.apk"
cp "$DEBUG_SOURCE" "$DEBUG_TARGET"
sha256sum "$DEBUG_TARGET" > "${DEBUG_TARGET}.sha256"

# Step 4: Sign, Align and Export Release Artifacts
echo "-> [4/5] Signing and aligning release APKs..."
RELEASE_TARGET="$EXPORT_DIR/orca-release.apk"
INSTALLABLE_TARGET="$EXPORT_DIR/orca-installable.apk"
LATEST_TARGET="$EXPORT_DIR/orca-release-latest.apk"

cp "$RELEASE_SOURCE" "$RELEASE_TARGET"

# Sign using sign.sh utility
if [ -f "$ROOT_DIR/scripts/sign.sh" ]; then
    cp "$RELEASE_SOURCE" "$INSTALLABLE_TARGET"
    "$ROOT_DIR/scripts/sign.sh" "$INSTALLABLE_TARGET"
    cp "$INSTALLABLE_TARGET" "$LATEST_TARGET"
elif [ -f "$ROOT_DIR/scripts/sign-apk.sh" ]; then
    "$ROOT_DIR/scripts/sign-apk.sh" "$RELEASE_SOURCE" "$INSTALLABLE_TARGET"
    cp "$INSTALLABLE_TARGET" "$LATEST_TARGET"
else
    cp "$RELEASE_SOURCE" "$INSTALLABLE_TARGET"
    cp "$RELEASE_SOURCE" "$LATEST_TARGET"
fi

sha256sum "$RELEASE_TARGET" > "${RELEASE_TARGET}.sha256"
sha256sum "$INSTALLABLE_TARGET" > "${INSTALLABLE_TARGET}.sha256"
md5sum "$INSTALLABLE_TARGET" > "${INSTALLABLE_TARGET}.md5"
sha256sum "$LATEST_TARGET" > "${LATEST_TARGET}.sha256"

# Step 5: Read Version and Generate Manifest
echo "-> [5/5] Generating release manifest..."
VERSION_CODE=$(grep -E 'versionCode\s*=\s*[0-9]+' app/build.gradle.kts | awk '{print $3}')
VERSION_NAME=$(grep -E 'versionName\s*=\s*"[^"]+"' app/build.gradle.kts | sed -E 's/.*"([^"]+)".*/\1/')
APP_ID=$(grep -E 'applicationId\s*=\s*"[^"]+"' app/build.gradle.kts | sed -E 's/.*"([^"]+)".*/\1/')
BUILD_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
APK_SIZE=$(stat -c%s "$INSTALLABLE_TARGET" 2>/dev/null || stat -f%z "$INSTALLABLE_TARGET" 2>/dev/null || wc -c < "$INSTALLABLE_TARGET")
SHA256_HASH=$(cat "${INSTALLABLE_TARGET}.sha256" | cut -d' ' -f1)

cat <<EOF > "$EXPORT_DIR/build-manifest.json"
{
  "appName": "Orca",
  "applicationId": "$APP_ID",
  "versionName": "$VERSION_NAME",
  "versionCode": $VERSION_CODE,
  "buildTime": "$BUILD_TIME",
  "debugApk": "orca-debug.apk",
  "releaseApk": "orca-release.apk",
  "installableApk": "orca-installable.apk",
  "latestReleaseApk": "orca-release-latest.apk",
  "fileSizeBytes": $APK_SIZE,
  "sha256": "$SHA256_HASH",
  "minSdk": 26,
  "targetSdk": 36,
  "installCommand": "adb install -r $EXPORT_DIR/orca-installable.apk"
}
EOF

# Copy versioned release name
cp "$INSTALLABLE_TARGET" "$EXPORT_DIR/orca-v${VERSION_NAME}-release.apk"
sha256sum "$EXPORT_DIR/orca-v${VERSION_NAME}-release.apk" > "$EXPORT_DIR/orca-v${VERSION_NAME}-release.apk.sha256"

echo "=========================================================="
echo "  ✓ BUILD & PACKAGING COMPLETE!"
echo "=========================================================="
echo "  Export Directory:  $EXPORT_DIR"
echo "  Installable APK:   $INSTALLABLE_TARGET ($(du -h "$INSTALLABLE_TARGET" | cut -f1))"
echo "  Release APK:       $RELEASE_TARGET ($(du -h "$RELEASE_TARGET" | cut -f1))"
echo "  Debug APK:         $DEBUG_TARGET ($(du -h "$DEBUG_TARGET" | cut -f1))"
echo "  Build Manifest:    $EXPORT_DIR/build-manifest.json"
echo ""
echo "  Install on connected device/emulator:"
echo "    adb install -r $INSTALLABLE_TARGET"
echo "=========================================================="
