---
alias: auto-updater-meldoc-md
title: Auto Updater Meldoc Md
---
---
title: Auto-Updater
alias: auto-updater
parentAlias: architecture-overview
workflow: draft
---

## Overview

The auto-update system spans two modules: update detection in `shared` and `composeApp`, and the actual update installation in the standalone `updater` module. The updater is a separate Compose Desktop executable that runs independently of the main app.

## Update Detection Flow

1. **Version check**: On startup, `main.kt` calls `UpdateCheckerRepository.checkForUpdate(currentVersion)`.
2. **GitHub API**: The repository queries GitHub Releases for `Config.GITHUB_RELEASE_REPO`:
   - **Prod** checks `CypheronX/L2LootClientAppReleases` (public, latest releases)
   - **Stage** checks `CypheronX/L2LootClientAppTest` (private, includes pre-releases)
   - **Dev** skips update checking (auto-updater disabled)
3. **Version comparison**: Parses semantic versions from tag names. Supports two tag formats:
   - `v1.2.3` — production releases
   - `stage-v1.2.3-abc123` — staging releases with commit SHA
4. **Asset resolution**: Looks for both `.msi` and `.zip` assets in the release. The ZIP is used for in-place updates; the MSI is offered as a direct download.

If a newer version is found, `UpdateInfo` is returned containing the version string, download URLs, and release notes.

## Update Notification

When `checkForUpdate()` returns a result, `AppNavigationRail` shows a badge on the Settings icon, and an `UpdateNotification` popup appears at the bottom-right of the window. The popup auto-dismisses after 10 seconds and offers:

- **Download** — launches the updater process
- **Release Notes** — opens the GitHub release page in the browser

## Updater Module (`updater/`)

The updater is a standalone Compose Desktop application packaged as a separate executable. It is embedded as a ZIP resource inside `composeApp` at build time.

### Build Integration

1. `updater/build.gradle.kts` produces a distributable executable
2. `composeApp/build.gradle.kts` has tasks:
   - `zipUpdaterForResources` — ZIPs the updater distribution
   - `copyUpdaterToResources` — copies the ZIP into `composeResources/files/updater/`
3. The MSI packaging tasks depend on these to ensure the updater is always bundled

### Launch Process (`UpdateInstaller.kt`)

1. Extracts the embedded updater ZIP to `%APPDATA%/{APP_NAME}/updater/` (skips if same version already extracted)
2. Resolves the main app's installation path by navigating from the running JAR location
3. Launches the updater executable with command-line arguments:
   - `--download-url` — ZIP download URL
   - `--install-path` — app installation directory
   - `--app-exe` — main app executable name
   - `--current-version` / `--new-version`
   - `--github-token` — for authenticated downloads (stage builds)
4. The main app then exits

### Installation Process (`UpdaterWindow.kt`)

The updater window shows progress through these states:

1. **Downloading** — downloads the update ZIP to a temp file, showing byte progress
2. **Extracting** — unzips to a temp directory
3. **Installing** — copies extracted files over the existing installation using `robocopy`
4. **Registry update** — updates the Windows registry `DisplayVersion` for the app's uninstall entry
5. **Completed** — relaunches the main app with `--skip-update-check` flag

Error handling displays the failure message and offers a Retry button.

## Flavor Behavior

| Flavor | Auto-update | Release repo | Pre-releases |
|--------|-------------|-------------|--------------|
| prod | Enabled | Public (L2LootClientAppReleases) | No |
| stage | Enabled | Private (L2LootClientAppTest) | Yes |
| dev | Disabled | — | — |

The `--skip-update-check` flag is passed when launching the app after an update to prevent an infinite update loop.
