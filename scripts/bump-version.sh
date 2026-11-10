#!/usr/bin/env bash
set -euo pipefail

# Orca Version Bumping Script
# Usage:
#   ./scripts/bump-version.sh [patch|minor|major|<specific_version>]
# Example:
#   ./scripts/bump-version.sh patch
#   ./scripts/bump-version.sh 1.2.0

GRADLE_FILE="app/build.gradle.kts"
BUMP_TYPE="${1:-patch}"

if [ ! -f "$GRADLE_FILE" ]; then
    echo "Error: $GRADLE_FILE not found!" >&2
    exit 1
fi

CURRENT_VERSION_CODE=$(grep -E 'versionCode\s*=\s*[0-9]+' "$GRADLE_FILE" | awk '{print $3}')
CURRENT_VERSION_NAME=$(grep -E 'versionName\s*=\s*"[^"]+"' "$GRADLE_FILE" | sed -E 's/.*"([^"]+)".*/\1/')

if [ -z "$CURRENT_VERSION_CODE" ] || [ -z "$CURRENT_VERSION_NAME" ]; then
    echo "Error: Could not parse current version from $GRADLE_FILE" >&2
    exit 1
fi

NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

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
            echo "Invalid version or bump type: $BUMP_TYPE. Expected 'major', 'minor', 'patch', or 'X.Y.Z'." >&2
            exit 1
        fi
        ;;
esac

echo "========================================"
echo " Orca Version Bump"
echo "========================================"
echo " Current Version: v$CURRENT_VERSION_NAME (Code: $CURRENT_VERSION_CODE)"
echo " New Version:     v$NEW_VERSION_NAME (Code: $NEW_VERSION_CODE)"
echo "========================================"

# Update app/build.gradle.kts
sed -i -E "s/versionCode\s*=\s*[0-9]+/versionCode = $NEW_VERSION_CODE/" "$GRADLE_FILE"
sed -i -E "s/versionName\s*=\s*\"[^\"]+\"/versionName = \"$NEW_VERSION_NAME\"/" "$GRADLE_FILE"

echo "✓ Successfully updated $GRADLE_FILE"
