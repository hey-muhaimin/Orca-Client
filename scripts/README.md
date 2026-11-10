# Orca Android Build & Packaging Scripts

This directory contains automation scripts to build, sign, package, bump versions, and export production-ready, installable APKs into `/exported_build`.

## Available Scripts

| Script | Purpose | Output Location |
| :--- | :--- | :--- |
| `sign.sh` | Signs generated APKs in `/exported_build` with `apksigner` and the keystore (v1/v2/v3 schemes), verifying signatures and generating checksums. | `/exported_build/*.apk` |
| `generate-installable-apk.sh` | **Turnkey master script** that compiles, zipaligns, signs with verification, and exports a ready-to-install APK. | `/exported_build/orca-installable.apk` |
| `build-debug.sh` | Compiles the debug build variant with logging and exports it. | `/exported_build/orca-debug.apk` |
| `build-release.sh` | Compiles the release build variant and exports it. | `/exported_build/orca-release.apk` |
| `sign-apk.sh` | Standalone utility to 4-byte zipalign and sign any APK with `apksigner` / `jarsigner`. | `/exported_build/<name>.apk` |
| `package-release.sh` | Full release pipeline creating versioned APKs, latest symlinks/copies, SHA-256 checksums, and `build-manifest.json`. | `/exported_build/orca-v<version>-release.apk` |
| `bump-version.sh` | Automatically bumps `versionCode` (+1) and `versionName` (patch/minor/major) in `app/build.gradle.kts`. | Updates `app/build.gradle.kts` |

---

## Quick Start

### 1. Generate Ready-to-Install APK in One Command
```bash
./scripts/generate-installable-apk.sh
```

The resulting signed APK is placed at:
```bash
/exported_build/orca-installable.apk
```

### 2. Install on Device or Emulator
```bash
adb install -r /exported_build/orca-installable.apk
```

### 3. Bump App Version
```bash
# Bump patch (1.0.0 -> 1.0.1)
./scripts/bump-version.sh patch

# Bump minor (1.0.1 -> 1.1.0)
./scripts/bump-version.sh minor

# Bump major (1.1.0 -> 2.0.0)
./scripts/bump-version.sh major

# Set exact version
./scripts/bump-version.sh 2.4.0
```

### 4. Custom Signing Options
By default, `sign-apk.sh` uses `debug.keystore` located in the root repository. You can pass your custom release keystore as arguments or environment variables:
```bash
./scripts/sign-apk.sh <input_apk> <output_apk> [keystore_path] [keystore_password] [key_alias] [key_password]
```
