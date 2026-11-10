#!/usr/bin/env bash
set -euo pipefail

# Orca Version Bumping Script
# Reads version.properties, increments versionCode & versionName,
# and synchronizes both version.properties and app/build.gradle.kts.
#
# Usage:
#   ./scripts/bump_version.sh [patch|minor|major|<x.y.z>]
# Example:
#   ./scripts/bump_version.sh
#   ./scripts/bump_version.sh minor
#   ./scripts/bump_version.sh 1.2.0

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

PROP_FILE="version.properties"
GRADLE_FILE="app/build.gradle.kts"
BUMP_TYPE="${1:-patch}"

if [ ! -f "$PROP_FILE" ]; then
    echo "Error: $PROP_FILE not found in $ROOT_DIR!" >&2
    exit 1
fi

if [ ! -f "$GRADLE_FILE" ]; then
    echo "Error: $GRADLE_FILE not found in $ROOT_DIR!" >&2
    exit 1
fi

# Read version.properties
CURRENT_VERSION_CODE=$(grep -E '^\s*VERSION_CODE\s*=' "$PROP_FILE" | cut -d'=' -f2 | tr -d '[:space:]')
CURRENT_VERSION_NAME=$(grep -E '^\s*VERSION_NAME\s*=' "$PROP_FILE" | cut -d'=' -f2 | tr -d '[:space:]')

if [ -z "$CURRENT_VERSION_CODE" ] || [ -z "$CURRENT_VERSION_NAME" ]; then
    echo "Error: Could not parse VERSION_CODE or VERSION_NAME from $PROP_FILE" >&2
    exit 1
fi

# Increment version code
NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

# Compute new version name
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION_NAME"
MAJOR="${MAJOR:-1}"
MINOR="${MINOR:-0}"
PATCH="${PATCH:-0}"

case "$BUMP_TYPE" in
    major)
        NEW_MAJOR=$((MAJOR + 1))
        NEW_VERSION_NAME="${NEW_MAJOR}.0.0"
        ;;
    minor)
        NEW_MINOR=$((MINOR + 1))
        NEW_VERSION_NAME="${MAJOR}.${NEW_MINOR}.0"
        ;;
    patch)
        NEW_PATCH=$((PATCH + 1))
        NEW_VERSION_NAME="${MAJOR}.${MINOR}.${NEW_PATCH}"
        ;;
    *)
        if [[ "$BUMP_TYPE" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
            NEW_VERSION_NAME="$BUMP_TYPE"
        else
            echo "Invalid bump type: '$BUMP_TYPE'. Expected 'patch', 'minor', 'major', or a version string like '1.2.3'." >&2
            exit 1
        fi
        ;;
esac

echo "=========================================================="
echo "  Orca - Version Bump"
echo "=========================================================="
echo "  Current Version:  v$CURRENT_VERSION_NAME (Code: $CURRENT_VERSION_CODE)"
echo "  Target Version:   v$NEW_VERSION_NAME (Code: $NEW_VERSION_CODE)"
echo "  Bump Strategy:    $BUMP_TYPE"
echo "=========================================================="

# 1. Update version.properties
cat <<EOF > "$PROP_FILE"
VERSION_CODE=$NEW_VERSION_CODE
VERSION_NAME=$NEW_VERSION_NAME
EOF
echo "✓ Updated $PROP_FILE"

# 2. Update app/build.gradle.kts
sed -i -E "s/versionCode\s*=\s*[0-9]+/versionCode = $NEW_VERSION_CODE/" "$GRADLE_FILE"
sed -i -E "s/versionName\s*=\s*\"[^\"]+\"/versionName = \"$NEW_VERSION_NAME\"/" "$GRADLE_FILE"
echo "✓ Updated $GRADLE_FILE"

echo "=========================================================="
echo "  Version bump successful: v$NEW_VERSION_NAME (Code: $NEW_VERSION_CODE)"
echo "=========================================================="
