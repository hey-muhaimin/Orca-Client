# Orca for Android

> **The AI Orchestrator for 100x Builders.**  
> Monitor, steer, and review autonomous coding agents running in parallel worktrees right from your Android device.

Orca orchestrates coding agents (Claude Code, OpenAI Codex, Cursor, Devin, Grok, DeepSeek, and Pi) in isolated worktrees across local machines, remote SSH hosts, and cloud sandboxes.

---

## Features

- **Parallel Worktrees**: Inspect, switch, and spawn isolated git worktrees across local daemons, SSH hosts, and cloud workers.
- **Dual Mode Agent Sessions**: Seamlessly toggle between **Chat UI** (with collapsible reasoning and interactive tool approval cards) and a **Terminal CLI** (monospace command prompt with real-time logs).
- **Tool Permission Approvals**: Review command execution requests (e.g. `pnpm test`, `git commit`) with one-tap Approve or Reject.
- **AI Diff Review & Line Annotations**: Inspect modified, added, and deleted files with syntax line numbers. Tap any line to attach inline feedback that feeds directly into the agent's prompt context.
- **Pull Request & Commit Generator**: Draft commit messages and open Pull Requests directly from the verified diffs.
- **Linear & GitHub Tasks**: Native issue tracking with one-tap "Start Worktree" to spin up an agent workspace preloaded with the issue context.
- **Remote Pairing & Token Limits**: Monitor desktop daemon pairing, network latency, active tunnels, and token usage meters across AI providers.
- **Packaging & APK Generation Scripts**: Automated build, sign, version bump, and release packaging scripts in `/scripts/` outputting to `/exported_build/`.

---

## Exported Builds & Installation

Ready-to-install APKs are generated and located in `/exported_build/`:

| File | Description |
| :--- | :--- |
| `/exported_build/orca-installable.apk` | **Ready-to-install release APK** (zipalign 4-byte optimized & signed with APK Signature Scheme v2/v3). |
| `/exported_build/orca-release-latest.apk` | Latest release APK package. |
| `/exported_build/orca-debug.apk` | Debug APK variant. |
| `/exported_build/build-manifest.json` | Release metadata, sha256 checksum, file size, and targetSdk. |

### Install via ADB:
```bash
adb install -r /exported_build/orca-installable.apk
```

---

## Build Scripts (`/scripts`)

- **Generate Installable APK**:
  ```bash
  ./scripts/generate-installable-apk.sh
  ```
- **Build Release APK**:
  ```bash
  ./scripts/build-release.sh
  ```
- **Build Debug APK**:
  ```bash
  ./scripts/build-debug.sh
  ```
- **Sign Any APK**:
  ```bash
  ./scripts/sign-apk.sh <input_apk> [output_apk]
  ```
- **Bump Version**:
  ```bash
  ./scripts/bump-version.sh patch   # or minor, major, or X.Y.Z
  ```
- **Package Release**:
  ```bash
  ./scripts/package-release.sh
  ```

---

## Tech Stack

- **Platform**: Android (minSdk 26, targetSdk 36, compileSdk 36)
- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose & Material Design 3
- **Build System**: Gradle 9.3.1 with Android Gradle Plugin 9.1.1
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
