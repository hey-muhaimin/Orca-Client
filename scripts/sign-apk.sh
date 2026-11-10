#!/usr/bin/env bash
set -euo pipefail

# Orca APK Signing Utility
# Usage:
#   ./scripts/sign-apk.sh <input_apk> [output_apk] [keystore_path] [keystore_pass] [key_alias] [key_pass]

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

INPUT_APK="${1:-app/build/outputs/apk/release/app-release-unsigned.apk}"
EXPORT_DIR="${EXPORT_DIR:-$ROOT_DIR/exported_build}"
mkdir -p "$EXPORT_DIR"

OUTPUT_APK="${2:-$EXPORT_DIR/orca-signed.apk}"
KEYSTORE="${3:-$ROOT_DIR/debug.keystore}"
KEYSTORE_PASS="${4:-android}"
KEY_ALIAS="${5:-androiddebugkey}"
KEY_PASS="${6:-android}"

# Detect SDK build tools
BUILD_TOOLS_DIR=$(find "${ANDROID_SDK_ROOT:-/opt/android/sdk}/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -n 1)
ZIPALIGN="$BUILD_TOOLS_DIR/zipalign"
APKSIGNER="$BUILD_TOOLS_DIR/apksigner"

if [ ! -f "$INPUT_APK" ]; then
    # Fallback to any release apk if unsigned is not found
    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        INPUT_APK="app/build/outputs/apk/release/app-release.apk"
    elif [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        INPUT_APK="app/build/outputs/apk/debug/app-debug.apk"
    else
        echo "Error: Input APK '$INPUT_APK' does not exist." >&2
        exit 1
    fi
fi

if [ ! -f "$KEYSTORE" ]; then
    echo "Error: Keystore '$KEYSTORE' not found." >&2
    exit 1
fi

echo "========================================"
echo " Signing APK with apksigner"
echo "========================================"
echo " Input:    $INPUT_APK"
echo " Output:   $OUTPUT_APK"
echo " Keystore: $KEYSTORE"
echo " Alias:    $KEY_ALIAS"
echo "========================================"

TMP_ALIGNED="/tmp/orca-aligned-$$.apk"
rm -f "$TMP_ALIGNED"

# 4-byte page align APK for modern Android runtime
if [ -x "$ZIPALIGN" ]; then
    echo "-> Running zipalign 4..."
    "$ZIPALIGN" -v -p 4 "$INPUT_APK" "$TMP_ALIGNED" > /dev/null
else
    echo "-> zipalign not found, using input as-is"
    cp "$INPUT_APK" "$TMP_ALIGNED"
fi

# Sign with apksigner
if [ -x "$APKSIGNER" ]; then
    echo "-> Signing with apksigner..."
    "$APKSIGNER" sign \
        --ks "$KEYSTORE" \
        --ks-pass "pass:$KEYSTORE_PASS" \
        --ks-key-alias "$KEY_ALIAS" \
        --key-pass "pass:$KEY_PASS" \
        --out "$OUTPUT_APK" \
        "$TMP_ALIGNED"

    echo "-> Verifying signature..."
    "$APKSIGNER" verify --verbose "$OUTPUT_APK"
else
    echo "-> apksigner not found, falling back to jarsigner..."
    jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
        -keystore "$KEYSTORE" \
        -storepass "$KEYSTORE_PASS" \
        -keypass "$KEY_PASS" \
        "$TMP_ALIGNED" "$KEY_ALIAS"
    mv "$TMP_ALIGNED" "$OUTPUT_APK"
fi

rm -f "$TMP_ALIGNED"

# Checksum
sha256sum "$OUTPUT_APK" > "${OUTPUT_APK}.sha256"

echo "========================================"
echo "✓ APK Successfully Signed & Verified!"
echo " Location: $OUTPUT_APK"
echo " Size:     $(du -h "$OUTPUT_APK" | cut -f1)"
echo " Install:  adb install -r $OUTPUT_APK"
echo "========================================"
