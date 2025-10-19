# Quick Start Guide - Auto-Update System

## Step 1: Build the Updater

Before you can test or use the auto-update system, you need to build the updater executable:

```bash
# Windows PowerShell
.\gradlew.bat :updater:packageReleaseExe copyUpdaterToResources
```

This will:
1. Build the updater application (`L2LootUpdater.exe`)
2. Copy it to `composeApp/src/jvmMain/composeResources/files/updater/`

**Verify**: Check that `L2LootUpdater.exe` exists in the updater directory.

## Step 2: Build Your App

Build your app with the desired flavor:

```bash
# Development build
.\gradlew.bat clean packageReleaseMsi zipAppUpdate -Pbuildkonfig.flavor=dev

# OR Production build
.\gradlew.bat clean packageReleaseMsi zipAppUpdate -Pbuildkonfig.flavor=prod
```

This creates:
- **MSI Installer**: `composeApp/build/compose/binaries/main-release/msi/`
- **Update ZIP**: `composeApp/build/compose/binaries/main-release/update/`

## Step 3: Test Locally (Optional)

To test the update system locally:

### 3.1 Create a "Current" Version

1. Update `version.properties` to version 1.0.0:
   ```properties
   versionMajor=1
   versionMinor=0
   versionPatch=0
   ```

2. Build and install:
   ```bash
   .\gradlew.bat clean packageReleaseMsi -Pbuildkonfig.flavor=dev
   ```

3. Install the MSI

### 3.2 Create a "New" Version

1. Update `version.properties` to version 1.0.1:
   ```properties
   versionMajor=1
   versionMinor=0
   versionPatch=1
   ```

2. Build:
   ```bash
   .\gradlew.bat clean packageReleaseMsi zipAppUpdate -Pbuildkonfig.flavor=dev
   ```

### 3.3 Create Test GitHub Release

1. Go to `aleksbalev/L2LootClientAppTest` (for dev) or `aleksbalev/L2LootClientAppReleases` (for prod)
2. Create a new release with tag `v1.0.1` or `dev-v1.0.1-test`
3. Upload both:
   - MSI file
   - Update ZIP file (must be named `*-Update-*.zip` or end with `.zip`)

### 3.4 Test Update Flow

1. Launch the installed v1.0.0 app
2. App should detect v1.0.1 from GitHub
3. Updater UI appears showing download progress
4. Update installs automatically
5. Updated app launches

## Step 4: Deploy via CI/CD

When you push a tag to GitHub, the workflow automatically:

### For Production (tag: `v*.*.*`)
1. Builds updater
2. Builds MSI and update ZIP
3. Uploads to `aleksbalev/L2LootClientAppReleases`

### For Development (branch: `test`)
1. Builds updater
2. Builds MSI and update ZIP
3. Uploads to `aleksbalev/L2LootClientAppTest` with tag `dev-v*.*.*-{sha}`

**To trigger a production release:**
```bash
git tag v1.1.0
git push origin v1.1.0
```

**To trigger a dev build:**
```bash
git checkout test
git push origin test
```

## Common Issues and Solutions

### Issue: Updater not found in resources

**Solution:**
```bash
.\gradlew.bat :updater:packageReleaseExe copyUpdaterToResources
```

### Issue: Update not detected

**Check:**
- Release exists in correct GitHub repository
- ZIP file is properly named (contains `-Update-` or ends with `.zip`)
- Version in tag is newer than current version
- Release is not a pre-release or draft

### Issue: App doesn't have updateZipUrl

**Check:**
- Rebuild shared module to pick up BuildKonfig changes:
  ```bash
  .\gradlew.bat :shared:clean :shared:build
  ```

### Issue: Build fails

**Try:**
1. Clean all builds:
   ```bash
   .\gradlew.bat clean
   ```

2. Rebuild from scratch:
   ```bash
   .\gradlew.bat :updater:packageReleaseExe copyUpdaterToResources
   .\gradlew.bat packageReleaseMsi zipAppUpdate -Pbuildkonfig.flavor=dev
   ```

## What Happens During Update

1. **App Launch**: User double-clicks `L2Loot.exe`
2. **Check**: App checks GitHub API (takes ~1-2 seconds)
3. **No Update**: App launches normally
4. **Update Available**: 
   - Updater UI appears
   - Downloads ZIP (progress shown)
   - Extracts files
   - Replaces app files
   - Launches updated app
   - Total time: ~30-60 seconds depending on internet speed

## Files to Check

Before committing, ensure these exist and are updated:

- [x] `updater/build.gradle.kts` - Updater build configuration
- [x] `updater/src/jvmMain/kotlin/com/l2loot/updater/Main.kt` - Updater entry point
- [x] `updater/src/jvmMain/kotlin/com/l2loot/updater/UpdaterWindow.kt` - Updater UI
- [x] `composeApp/src/jvmMain/composeResources/files/updater/L2LootUpdater.exe` - Built executable
- [x] `composeApp/src/jvmMain/kotlin/com/l2loot/CheckForUpdatesOnStartup.kt` - Startup check
- [x] `shared/src/jvmMain/kotlin/com/l2loot/update/UpdateInstaller.kt` - Installer logic
- [x] `shared/build.gradle.kts` - BuildKonfig with GITHUB_RELEASE_REPO
- [x] `.github/workflows/main.yml` - CI/CD with updater build steps

## Next Steps

1. **Build the updater** (Step 1 above) - **REQUIRED**
2. **Test locally** (Steps 2-3) - Recommended before pushing
3. **Push to test branch** to verify CI/CD workflow
4. **Create a production release** once tested

## Need Help?

See `AUTO_UPDATE_IMPLEMENTATION.md` for detailed architecture and troubleshooting.

