# Update Checker Setup Guide

## Overview

Your app now automatically checks for updates on startup from your public GitHub repository (`aleksbalev/L2LootClientAppReleases`) and displays a notification when a new version is available.

## How It Works

1. **On app startup** (after 2 seconds delay):
   - Fetches the latest release from GitHub Releases API
   - Compares the latest version with your current app version
   - Shows a notification in the bottom-right corner if an update is available

2. **The notification**:
   - Shows the new version number
   - Has a "Download" button (opens the MSI download link)
   - Has a "Release Notes" button (opens the GitHub release page)
   - Auto-dismisses after 10 seconds
   - Can be manually closed with the X button

3. **Version comparison**:
   - Uses semantic versioning (1.2.3)
   - Compares Major.Minor.Patch numbers
   - Only shows notification if remote version is newer

## Important: Keep Version in Sync

When you release a new version, you need to update the version in **TWO** places:

### 1. version.properties
```properties
versionMajor=1
versionMinor=0
versionPatch=1
```

### 2. shared/src/commonMain/kotlin/com/l2loot/BuildConfig.kt
```kotlin
const val VERSION_NAME = "1.0.1"
```

### Quick Helper Script

Run this script after updating `version.properties` to automatically sync BuildConfig:
```bash
update-version.bat
```

## Release Process

1. **Update version**:
   ```bash
   # Edit version.properties
   versionMajor=1
   versionMinor=1
   versionPatch=0
   
   # Run helper script
   update-version.bat
   ```

2. **Commit and tag**:
   ```bash
   git add version.properties shared/src/commonMain/kotlin/com/l2loot/BuildConfig.kt
   git commit -m "Bump version to 1.1.0"
   git tag v1.1.0
   git push origin v1.1.0
   ```

3. **GitHub Actions automatically**:
   - Builds the MSI installer
   - Creates a release in your public repo
   - Uploads the installer

4. **Users**:
   - Open the app
   - See the update notification
   - Click "Download" to get the new version

## Customization

### Change update check delay
In `App.kt`, line ~197:
```kotlin
delay(2000) // Wait 2 seconds after startup
```

### Change notification auto-dismiss time
In `UpdateNotification.kt`, line ~28:
```kotlin
delay(10000) // Auto-dismiss after 10 seconds
```

### Change GitHub repository
In `shared/src/jvmMain/kotlin/com/l2loot/data/update/UpdateChecker.kt`, line ~22:
```kotlin
private val githubRepo: String = "aleksbalev/L2LootClientAppReleases"
```

## Testing

To test the update checker:

1. Set a lower version in BuildConfig (e.g., "0.9.0")
2. Run the app
3. Wait 2 seconds
4. You should see the update notification

## Troubleshooting

**Update notification doesn't appear:**
- Check console for error messages
- Verify GitHub repo is public
- Verify a release exists in the public repo
- Check BuildConfig.VERSION_NAME is correct

**Version comparison not working:**
- Ensure tag names in GitHub use format: `v1.0.0` (with 'v' prefix)
- Ensure BuildConfig.VERSION_NAME uses format: `1.0.0` (without 'v')

## Files Modified/Created

- ✅ `shared/src/jvmMain/kotlin/com/l2loot/data/update/UpdateChecker.kt` - Update checking service
- ✅ `shared/src/commonMain/kotlin/com/l2loot/BuildConfig.kt` - Version constant
- ✅ `shared/src/jvmMain/kotlin/com/l2loot/di/JvmModule.kt` - Koin DI registration
- ✅ `composeApp/src/jvmMain/kotlin/com/l2loot/ui/components/UpdateNotification.kt` - UI notification
- ✅ `composeApp/src/jvmMain/kotlin/com/l2loot/App.kt` - Integration
- ✅ `composeApp/proguard-rules.pro` - ProGuard rules
- ✅ `update-version.bat` - Helper script

## Benefits

✅ Users stay up-to-date without manually checking
✅ No server or backend required
✅ Uses free GitHub infrastructure
✅ Non-intrusive notification
✅ Direct download link

