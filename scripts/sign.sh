#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# Orca APK Signing & Distribution Preparation Script
#
# Uses the Android SDK 'apksigner' tool to sign generated APKs in /exported_build
# with the keystore (supporting v1, v2, and v3 signature schemes), verifies the
# signatures, and updates checksums so APKs are ready for distribution.
#
# Usage:
#   ./scripts/sign.sh [OPTIONS] [APK_FILE]
#
# Options:
#   -k, --keystore <path>       Path to signing keystore (default: $ROOT_DIR/debug.keystore)
#   -p, --ks-pass <password>    Keystore password (default: android)
#   -a, --alias <name>          Key alias (default: androiddebugkey)
#   --key-pass <password>       Key password (default: android)
#   -d, --dir <directory>       Target exported build directory (default: $ROOT_DIR/exported_build)
#   --all                       Sign all APK files found in exported_build
#   -h, --help                  Show this help message
# ==============================================================================

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
EXPORT_DIR="${EXPORT_DIR:-$ROOT_DIR/exported_build}"
KEYSTORE="${KEYSTORE:-$ROOT_DIR/debug.keystore}"
KEYSTORE_PASS="${KEYSTORE_PASS:-android}"
KEY_ALIAS="${KEY_ALIAS:-androiddebugkey}"
KEY_PASS="${KEY_PASS:-android}"
TARGET_SPECIFIED=""
SIGN_ALL=false

# ANSI color codes
BOLD='\033[1m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

show_help() {
    cat << EOF
Orca APK Signing Tool (apksigner)

Usage:
  ./scripts/sign.sh [OPTIONS] [APK_FILE]

Arguments:
  APK_FILE                    Specific APK in /exported_build to sign (optional)

Options:
  -k, --keystore PATH         Path to keystore file (default: ./debug.keystore)
  -p, --ks-pass PASSWORD      Keystore password (default: 'android' or \$KEYSTORE_PASS)
  -a, --alias NAME            Key alias in keystore (default: 'androiddebugkey' or \$KEY_ALIAS)
  --key-pass PASSWORD         Key password (default: 'android' or \$KEY_PASS)
  -d, --dir PATH              Export directory (default: ./exported_build)
  --all                       Sign all APKs found in the export directory
  -h, --help                  Display this help message

Examples:
  ./scripts/sign.sh
  ./scripts/sign.sh /exported_build/orca-release.apk
  ./scripts/sign.sh --all
  ./scripts/sign.sh --keystore /path/to/release.keystore --alias myreleasekey
EOF
}

# Parse command line flags
while [[ $# -gt 0 ]]; do
    case "$1" in
        -k|--keystore)
            KEYSTORE="$2"
            shift 2
            ;;
        -p|--ks-pass)
            KEYSTORE_PASS="$2"
            shift 2
            ;;
        -a|--alias)
            KEY_ALIAS="$2"
            shift 2
            ;;
        --key-pass)
            KEY_PASS="$2"
            shift 2
            ;;
        -d|--dir)
            EXPORT_DIR="$2"
            shift 2
            ;;
        --all)
            SIGN_ALL=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            if [ -z "$TARGET_SPECIFIED" ]; then
                TARGET_SPECIFIED="$1"
                shift
            else
                echo -e "${RED}Unknown argument: $1${NC}" >&2
                show_help
                exit 1
            fi
            ;;
    esac
done

echo -e "${BOLD}${BLUE}==========================================================${NC}"
echo -e "${BOLD}${BLUE}  Orca APK Signing Tool (apksigner)${NC}"
echo -e "${BOLD}${BLUE}==========================================================${NC}"

# Locate Android build-tools binaries
BUILD_TOOLS_DIR=$(find "${ANDROID_SDK_ROOT:-/opt/android/sdk}/build-tools" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | sort -V | tail -n 1 || true)
APKSIGNER=""
ZIPALIGN=""

if command -v apksigner >/dev/null 2>&1; then
    APKSIGNER=$(command -v apksigner)
elif [ -n "$BUILD_TOOLS_DIR" ] && [ -x "$BUILD_TOOLS_DIR/apksigner" ]; then
    APKSIGNER="$BUILD_TOOLS_DIR/apksigner"
fi

if command -v zipalign >/dev/null 2>&1; then
    ZIPALIGN=$(command -v zipalign)
elif [ -n "$BUILD_TOOLS_DIR" ] && [ -x "$BUILD_TOOLS_DIR/zipalign" ]; then
    ZIPALIGN="$BUILD_TOOLS_DIR/zipalign"
fi

if [ -z "$APKSIGNER" ]; then
    echo -e "${RED}Error: 'apksigner' tool not found in PATH or Android SDK build-tools.${NC}" >&2
    exit 1
fi

echo -e "Using apksigner: ${GREEN}$APKSIGNER${NC}"
if [ -n "$ZIPALIGN" ]; then
    echo -e "Using zipalign:  ${GREEN}$ZIPALIGN${NC}"
fi

# Validate Keystore
if [ ! -f "$KEYSTORE" ]; then
    echo -e "${RED}Error: Keystore file not found at '$KEYSTORE'.${NC}" >&2
    exit 1
fi
echo -e "Using keystore:  ${GREEN}$KEYSTORE${NC} (alias: ${KEY_ALIAS})"
echo -e "Target directory: ${GREEN}$EXPORT_DIR${NC}"
echo -e "${BOLD}${BLUE}==========================================================${NC}\n"

if [ ! -d "$EXPORT_DIR" ]; then
    echo -e "${RED}Error: Export directory '$EXPORT_DIR' does not exist.${NC}" >&2
    exit 1
fi

# Determine which APKs to sign
APKS_TO_SIGN=()

if [ -n "$TARGET_SPECIFIED" ]; then
    if [ -f "$TARGET_SPECIFIED" ]; then
        APKS_TO_SIGN+=("$TARGET_SPECIFIED")
    elif [ -f "$EXPORT_DIR/$TARGET_SPECIFIED" ]; then
        APKS_TO_SIGN+=("$EXPORT_DIR/$TARGET_SPECIFIED")
    else
        echo -e "${RED}Error: Specified APK file '$TARGET_SPECIFIED' was not found.${NC}" >&2
        exit 1
    fi
elif [ "$SIGN_ALL" = true ]; then
    while IFS= read -r -d '' file; do
        APKS_TO_SIGN+=("$file")
    done < <(find "$EXPORT_DIR" -maxdepth 1 -name "*.apk" -print0)
else
    # Default behavior: sign standard distribution APKs in exported_build
    CANDIDATES=(
        "$EXPORT_DIR/orca-installable.apk"
        "$EXPORT_DIR/orca-release.apk"
        "$EXPORT_DIR/orca-release-latest.apk"
    )
    for cand in "${CANDIDATES[@]}"; do
        if [ -f "$cand" ]; then
            APKS_TO_SIGN+=("$cand")
        fi
    done

    # Also include any versioned release APKs (e.g., orca-v1.0.1-release.apk)
    while IFS= read -r -d '' file; do
        if [[ ! " ${APKS_TO_SIGN[*]} " =~ " ${file} " ]]; then
            APKS_TO_SIGN+=("$file")
        fi
    done < <(find "$EXPORT_DIR" -maxdepth 1 -name "orca-v*-release.apk" -print0)

    # Fallback to all APKs in export dir if none of the above matched
    if [ ${#APKS_TO_SIGN[@]} -eq 0 ]; then
        while IFS= read -r -d '' file; do
            APKS_TO_SIGN+=("$file")
        done < <(find "$EXPORT_DIR" -maxdepth 1 -name "*.apk" -print0)
    fi
fi

if [ ${#APKS_TO_SIGN[@]} -eq 0 ]; then
    echo -e "${YELLOW}Warning: No APK files found in '$EXPORT_DIR' to sign.${NC}"
    echo "Run './scripts/build.sh' first to generate APK artifacts."
    exit 0
fi

echo -e "${BOLD}Found ${#APKS_TO_SIGN[@]} APK artifact(s) to sign:${NC}"
for apk in "${APKS_TO_SIGN[@]}"; do
    echo "  - $(basename "$apk")"
done
echo ""

# Function to sign a single APK using apksigner
sign_apk() {
    local apk_path="$1"
    local apk_name
    apk_name=$(basename "$apk_path")

    echo -e "${BOLD}${BLUE}----------------------------------------------------------${NC}"
    echo -e "${BOLD}Signing: ${GREEN}$apk_name${NC}"
    echo -e "${BOLD}Path:    $apk_path${NC}"

    local tmp_aligned="/tmp/align_${$}_${apk_name}"
    rm -f "$tmp_aligned"

    # Step A: 4-byte page align using zipalign if available
    if [ -n "$ZIPALIGN" ]; then
        # Check if already 4-byte aligned
        if "$ZIPALIGN" -c -v 4 "$apk_path" >/dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} APK is already 4-byte page aligned."
            cp "$apk_path" "$tmp_aligned"
        else
            echo "  -> Running 4-byte page alignment (zipalign)..."
            "$ZIPALIGN" -f -p 4 "$apk_path" "$tmp_aligned"
            echo -e "  ${GREEN}✓${NC} 4-byte page alignment complete."
        fi
    else
        cp "$apk_path" "$tmp_aligned"
    fi

    # Step B: Sign with apksigner (v1, v2, and v3 signature schemes)
    echo "  -> Signing with apksigner (v1, v2, v3)..."
    "$APKSIGNER" sign \
        --ks "$KEYSTORE" \
        --ks-pass "pass:$KEYSTORE_PASS" \
        --ks-key-alias "$KEY_ALIAS" \
        --key-pass "pass:$KEY_PASS" \
        --v1-signing-enabled true \
        --v2-signing-enabled true \
        --v3-signing-enabled true \
        --out "$apk_path" \
        "$tmp_aligned"

    rm -f "$tmp_aligned"

    # Step C: Verify signature with apksigner
    echo "  -> Verifying APK signature integrity..."
    local verify_output
    verify_output=$("$APKSIGNER" verify --verbose "$apk_path" 2>&1)
    
    local v1_status
    local v2_status
    local v3_status
    v1_status=$(echo "$verify_output" | grep "Verified using v1 scheme" || echo "v1: unknown")
    v2_status=$(echo "$verify_output" | grep "Verified using v2 scheme" || echo "v2: unknown")
    v3_status=$(echo "$verify_output" | grep "Verified using v3 scheme" || echo "v3: unknown")

    echo -e "  ${GREEN}✓${NC} $v1_status"
    echo -e "  ${GREEN}✓${NC} $v2_status"
    echo -e "  ${GREEN}✓${NC} $v3_status"

    # Step D: Update checksums
    local size_human
    size_human=$(du -h "$apk_path" | cut -f1)
    sha256sum "$apk_path" > "${apk_path}.sha256"
    md5sum "$apk_path" > "${apk_path}.md5"

    echo -e "  ${GREEN}✓${NC} Updated checksums: ${apk_name}.sha256 & ${apk_name}.md5"
    echo -e "  ${GREEN}✓${NC} Final size: $size_human"
    echo -e "  ${GREEN}✓ Successfully signed and ready for distribution!${NC}"
}

# Sign each selected APK
for apk in "${APKS_TO_SIGN[@]}"; do
    sign_apk "$apk"
done

echo -e "\n${BOLD}${BLUE}==========================================================${NC}"
echo -e "${BOLD}${GREEN}  ✓ ALL APKS SUCCESSFULLY SIGNED & VERIFIED!${NC}"
echo -e "${BOLD}${BLUE}==========================================================${NC}"
echo "Distribution directory: $EXPORT_DIR"
echo ""
echo "Ready-to-install package:"
echo -e "  ${BOLD}$EXPORT_DIR/orca-installable.apk${NC}"
echo ""
echo "Installation command:"
echo "  adb install -r $EXPORT_DIR/orca-installable.apk"
echo -e "${BOLD}${BLUE}==========================================================${NC}"
