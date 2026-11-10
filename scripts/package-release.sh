#!/usr/bin/env bash
set -euo pipefail

# Orca Full Release Packaging Pipeline
# 1. Inspects/bumps version
# 2. Builds release APK
# 3. Zipaligns and signs
# 4. Exports versioned and latest APKs to /exported_build/
# 5. Generates SHA256 checksums and build-manifest.json

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

EXPORT_DIR="${EXPORT_DIR:-$ROOT_DIR/exported_build}"
mkdir -p "$EXPORT_DIR"

GRADLE_FILE="app/build.gradle.kts"
VERSION_CODE=$(grep -E 'versionCode\s*=\s*[0-9]+' "$GRADLE_FILE" | awk '{print $3}')
VERSION_NAME=$(grep -E 'versionName\s*=\s*"[^"]+"' "$GRADLE_FILE" | sed -E 's/.*"([^"]+)".*/\1/')
APP_ID=$(grep -E 'applicationId\s*=\s*"[^"]+"' "$GRADLE_FILE" | sed -E 's/.*"([^"]+)".*/\1/')

echo "========================================"
echo " Packaging Orca Release v$VERSION_NAME (Code: $VERSION_CODE)"
echo " Package ID: $APP_ID"
echo " Export Directory: $EXPORT_DIR"
echo "========================================"

# Step 1: Assemble Release
echo "-> Compiling and assembling release variant..."
gradle :app:assembleRelease

RELEASE_SOURCE="app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$RELEASE_SOURCE" ]; then
    RELEASE_SOURCE="app/build/outputs/apk/release/app-release-unsigned.apk"
fi

if [ ! -f "$RELEASE_SOURCE" ]; then
    echo "Error: Release build output not found!" >&2
    exit 1
fi

# Step 2: Sign & Align into Export Directory
VERSIONED_APK="$EXPORT_DIR/orca-v${VERSION_NAME}-release.apk"
LATEST_APK="$EXPORT_DIR/orca-release-latest.apk"

"$ROOT_DIR/scripts/sign-apk.sh" "$RELEASE_SOURCE" "$VERSIONED_APK"
cp "$VERSIONED_APK" "$LATEST_APK"
sha256sum "$LATEST_APK" > "${LATEST_APK}.sha256"

# Step 3: Metadata Manifest
BUILD_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
APK_SIZE_BYTES=$(stat -c%s "$VERSIONED_APK" 2>/dev/null || stat -f%z "$VERSIONED_APK" 2>/dev/null || wc -c < "$VERSIONED_APK")
SHA256=$(sha256sum "$VERSIONED_APK" | awk '{print $1}')

cat <<EOF > "$EXPORT_DIR/build-manifest.json"
{
  "appName": "Orca",
  "applicationId": "$APP_ID",
  "versionName": "$VERSION_NAME",
  "versionCode": $VERSION_CODE,
  "buildTime": "$BUILD_TIME",
  "apkFileName": "orca-v${VERSION_NAME}-release.apk",
  "latestFileName": "orca-release-latest.apk",
  "fileSizeBytes": $APK_SIZE_BYTES,
  "sha256": "$SHA256",
  "minSdk": 26,
  "targetSdk": 36,
  "installCommand": "adb install -r $EXPORT_DIR/orca-release-latest.apk"
}
EOF

echo "========================================"
echo "✓ Release Packaging Complete!"
echo " Versioned APK: $VERSIONED_APK"
echo " Latest APK:    $LATEST_APK"
echo " Manifest:      $EXPORT_DIR/build-manifest.json"
echo " Install with:  adb install -r $LATEST_APK"
echo "========================================"
